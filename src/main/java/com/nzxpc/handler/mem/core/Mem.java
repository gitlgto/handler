package com.nzxpc.handler.mem.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Bytes;
import com.nzxpc.handler.mem.core.entity.DefaultEventModel;
import com.nzxpc.handler.mem.core.entity.Event;
import com.nzxpc.handler.mem.core.entity.EventModelBase;
import com.nzxpc.handler.mem.core.entity.Result;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Getter
@Setter
@Accessors(chain = true)
public class Mem {
    public static Mem Instance = new Mem();
    private static RandomAccessFile file;
    private static final long MAX_SIZE = 1024 * 1024 * 100;
    private static final String EVENT = ".event";
    private static final String SNAP = ".snap";
    private static final String BAK = ".bak";
    private static final String DATE_PATTERN = "yyMMdd_HHmmss";
    private static String CurrentFileName;
    private static ContainerBase Container;
    private static Class<?> DataContainerClass;
    private static Map<String, Class<?>> typeModelMap = new HashMap<>();
    private static Map<String, IHandler> typeHandlerMap = new HashMap<>();

    private static <T extends ContainerBase> void backupSnapshot(T container) throws Exception {
        LocalDateTime nowTime = LocalDateTime.now();
        String currentFileName = nowTime.format(DateTimeFormatter.ofPattern(DATE_PATTERN));
        deleteIfExist(currentFileName, BAK);
        new ObjectMapper().findAndRegisterModules().writeValue(new File(currentFileName + BAK), container);

    }

    private static HashSet<String> EntityPackages = new HashSet<>();

    static void checkClass(Class clazz) {
        if (!EntityPackages.contains(clazz.getPackageName())) {
            System.out.println(clazz.getName() + "不是实体类，不能用来生成Id,支持的实体包有:" + EntityPackages);
        }
    }

    @SuppressWarnings("ALL")
    public static <T extends ContainerBase> T container(Class<T> clz) {
        return (T) Container;
    }

    public static <T1 extends ContainerBase, T2 extends DbSaverBase> Mem newContainer(Class<T1> containerClazz) {
        try {
            Container = containerClazz.getDeclaredConstructor().newInstance();
        } catch (Throwable e) {
            System.exit(0);
        }
        DataContainerClass = containerClazz;
        return Instance;
    }

    public static <T1 extends ContainerBase, T2 extends DbSaverBase> Mem newContainer(Class<T1> containerClazz, Class<T2> dbSaverClass) {
        ContainerChecker.check(containerClazz, dbSaverClass);
        return newContainer(containerClazz);
    }

    private static String HANDLER_PACKAGE;
    private static String DTO_PACKAGE;

    public <T extends IHandler> Mem init(Class<T> handlerCls, ICache iCache, Class<?>... entityPkgCls) throws Exception {
        removeOldFiles();
        if (DataContainerClass == null) {
            System.out.println("请先调用EsManager.newContainer方法");
            System.exit(0);
        }
        HANDLER_PACKAGE = handlerCls.getPackageName();
        DTO_PACKAGE = HANDLER_PACKAGE + ".dto";
        for (Class<?> clz : entityPkgCls) {
            EntityPackages.add(clz.getPackageName());
        }
        //coinIn实体类
        registerShutdownHook();

        return Instance;
    }

    private static Path getNewestFile(String fileType) {

        try (Stream<Path> stream = Files.walk(Paths.get("."))) {
            return stream.filter(a -> a.toString().endsWith(fileType)).max(Comparator.comparing(Path::toString)).orElse(null);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void removeOldFiles() {
        try {
            Path newestSnap = getNewestFile(SNAP);
            Path newestEvent = getNewestFile(EVENT);
            Date date = DateUtils.addDays(new Date(), -7);
            File targetDir = new File(",");
            Iterator<File> iterator = FileUtils.iterateFiles(targetDir, new AgeFileFilter(date), null);
            while (iterator.hasNext()) {
                File file = iterator.next();
                if (file.isFile()) {
                    boolean canDelete = false;
                    if (file.getName().endsWith(BAK) || file.getName().endsWith(".fall") || file.getName().startsWith("error.log")) {
                        canDelete = true;
                    } else if (file.getName().endsWith(SNAP)) {
                        canDelete = newestSnap != null && !file.getName().equals(newestSnap.toFile().getName());
                    } else if (file.getName().endsWith(EVENT)) {
                        canDelete = newestEvent != null && !file.getName().equals(newestEvent.toFile().getName());
                    }
                    if (canDelete) {
                        FileUtils.deleteQuietly(file);
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    private static void dealEventInFile(RandomAccessFile rf, Consumer<EventModelBase> consumer) throws Exception {
        int typeLen;
        int modelLen;
        while (true) {
            try {
                typeLen = rf.readInt();
                modelLen = rf.readInt();
            } catch (EOFException e) {
                break;
            }
            byte[] typeBuf = new byte[typeLen];
            byte[] modelBuf = new byte[modelLen];
            rf.read(typeBuf);
            rf.read(modelBuf);
            EventModelBase event = Mem.toEvent(typeBuf, modelBuf);
            consumer.accept(event);
        }
    }

    private static EventModelBase toEvent(byte[] typeBuf, byte[] modelBuf) throws IOException {
        String type = new String(typeBuf, StandardCharsets.ISO_8859_1);
        Class<?> clazz = typeModelMap.get(type);
        if (clazz == null) {
            clazz = DefaultEventModel.class;
        }
        return (EventModelBase) new ObjectMapper().findAndRegisterModules().readValue(modelBuf, clazz);
    }

    private static void restore(File file, Class<?> dataContainerClass) throws IOException {
        Container = (ContainerBase) new ObjectMapper().findAndRegisterModules().readValue(file, dataContainerClass);
        IdHolder.IdMap = Container.IdMap;
    }

    private static void snapshot() {
        try {
            if (file != null) {
                file.close();
                file = null;
            }
            CurrentFileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_PATTERN));
            deleteIfExist(CurrentFileName, SNAP);
            new ObjectMapper().findAndRegisterModules().writeValue(new File(CurrentFileName + SNAP), Container);
            deleteIfExist(CurrentFileName, EVENT);
            file = new RandomAccessFile(CurrentFileName + EVENT, "rw");
            file.seek(file.length());
        } catch (IOException e) {
            System.exit(0);
        }

    }

    private static RandomAccessFile createOrOpenEventFile(List<File> files, String snapFileName) throws IOException {
        LocalDateTime now = LocalDateTime.now();
        if (files.size() > 0) {
            File lastFile = files.get(files.size() - 1);
            if (lastFile.length() <= MAX_SIZE) {
                CurrentFileName = StringUtils.replace(lastFile.getName(), EVENT, "");
            } else {
                CurrentFileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_PATTERN));

            }
        } else {
            CurrentFileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_PATTERN));
        }
        if (StringUtils.isNotBlank(snapFileName) && CurrentFileName.compareTo(snapFileName) < 0) {
            LocalDateTime time = LocalDateTime.parse(snapFileName, DateTimeFormatter.ofPattern(DATE_PATTERN));
            CurrentFileName = time.format(DateTimeFormatter.ofPattern(DATE_PATTERN));
        }
        RandomAccessFile rf = new RandomAccessFile(CurrentFileName + EVENT, "rw");
        rf.seek(rf.length());
        return rf;

    }

    @AllArgsConstructor
    private static class PrepareBufResult {
        public byte[] type;
        public byte[] data;
    }

    private static void saveEvent(EventModelBase event) throws Exception {
        PrepareBufResult result = prepareBuf(event);
        byte[] all = Bytes.concat(result.data, result.type);
        file.writeInt(result.data.length);
        file.writeInt(result.type.length);
        file.write(all);
        if (file.length() > MAX_SIZE && !(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_PATTERN)).equals(CurrentFileName))) {
            file.close();
            CurrentFileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_PATTERN));
            file = new RandomAccessFile(CurrentFileName + EVENT, "rw");
        }
    }

    @SuppressWarnings("ALL")
    static void doEvent(EventModelBase event, boolean isRuntime) {
        Result result;
        if (isRuntime) {
            if (event.getNow() <= 0) {
                event.setNow(LocalDateTime.now().toInstant(OffsetDateTime.now().getOffset()).toEpochMilli());
            }
            event.setNano(System.nanoTime());
        }
        try {
            IHandler handler = getHandler(event.getType());
            if (isRuntime) {
                result = handler.process(event, isRuntime);
                event.setOk(result.isOk()).setMsg(result.getMsg()).setCode(result.getCode()).setExtend(result.getExtend());
            } else {
                result = handler.process(event, isRuntime);
            }
            if (result.isOk() && isRuntime && !ignoreEvents.contains(event.getType())) {
                Mem.saveEvent(event);
            }
        } catch (Throwable e) {
            System.out.println(event);
            System.exit(0);
        }
    }

    public static void end() {
        ContainerChecker.fall();
        Mem.eventToDb(true);
        removeOldFiles();
    }

    public static void eventToDb(boolean makeSnapshot) {
        System.out.println("开始事件入库");
        try {
            file.seek(0);
            int batchSize = 3000;
            List<Event> batchList = new ArrayList<>(batchSize);
            //数据库操作
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Set<String> ignoreEvents = new HashSet<>();

    private static IHandler getHandler(String type) {
        return typeHandlerMap.get(type);
    }

    private static PrepareBufResult prepareBuf(EventModelBase model) {
        String eventType;
        if (model.getClass() == DefaultEventModel.class) {
            eventType = model.getType();
            if (eventType == null) {
                System.out.println("缺少事件类型");
                System.exit(0);
            }
        } else {
            eventType = model2Type(model.getClass(), false);
        }

        byte[] type = eventType.getBytes(StandardCharsets.ISO_8859_1);
        byte[] data = null;
        try {
            data = new ObjectMapper().findAndRegisterModules().writeValueAsBytes(model);
        } catch (JsonProcessingException e) {
            System.exit(0);
            e.printStackTrace();
        }
        return new PrepareBufResult(type, data);
    }

    static String model2Type(Class modelClass, boolean check) {
        String name = modelClass.getSimpleName();
        if (check && !name.endsWith("Model")) {
            System.out.println(modelClass.getName() + "名称必须以Model为后缀");
            System.exit(0);
            return null;
        }
        return name.substring(0, name.length() - 5);
    }

    private static void deleteIfExist(String fileName, String extension) {
        fileName = fileName + extension;
        File file = new File(fileName);
        if (file.exists() || file.isDirectory()) {
            if (!file.delete()) {
                throw new RuntimeException("删除文件" + fileName + "失败");
            }
        }
    }

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Mem.backupSnapshot(Container);
                System.out.println("退出前生成快照备份");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }

}
