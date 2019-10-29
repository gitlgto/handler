package com.nzxpc.handler.util;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UploadUtil {
    private static String imgSiteDomain = AppPropUtil.get("img.site.domain");
    private static String imgDir = AppPropUtil.get("img.dir");

    private final static String accessKeyId = AppPropUtil.get("aliyun.oss.accessKeyId");
    private final static String secretAccessKey = AppPropUtil.get("aliyun.oss.accessKeySecret");
    public final static String endpoint = AppPropUtil.get("aliyun.oss.endpoint");
    public final static String bucketName = AppPropUtil.get("aliyun.oss.bucketname");

    private static boolean isLocal = StringUtils.indexOf(imgSiteDomain, "@") == -1
            && StringUtils.isNotBlank(imgSiteDomain)
            && StringUtils.indexOf(imgDir, "@") == -1
            && StringUtils.isNotBlank(imgDir);

    static {
        if (isLocal) {
            imgDir = Paths.get(imgDir).toString();
        }
    }

    private static String endpointInternal = AppPropUtil.get("aliyun.oss.endpoint.internal");

    private static String connectKey(String prefix, String fileName) {
        return prefix + fileName;
    }

    private static String getFullUrl(String key) {
        if (isLocal) {
            return String.format("//%s%s", imgSiteDomain, key.replace('\\', '/'));
        } else {
            return String.format("//%s.%s/%s", bucketName, endpoint, key.replace('\\', '/'));
        }
    }

    @Deprecated
    public static String uploadReturnFullUrl(String prefix, MultipartFile file) {
        return upReturnFullUrl(prefix, file);
    }

    public static String uploadReturnFullUrl(MultipartFile file) {
        return upReturnFullUrl("asset", file);
    }

    private static String upReturnFullUrl(String prefix, MultipartFile file) {
        if (isLocal) {
            String url = uploadToLocal(prefix, file);
            if (StringUtils.isNotEmpty(url)) {
                return getFullUrl(url);
            }
        } else {

        }
        throw new RuntimeException("上传图片失败，指定路径不存在");
    }

    private static String uploadToLocal(String prefix, MultipartFile file) {

        try {
            if (file.getSize() == 0) {
                LogUtil.err(prefix + "上传图片为空");
                return null;
            }
            return uploadToLocal(prefix, file.getOriginalFilename(), file.getInputStream());

        } catch (Exception e) {
            LogUtil.err("上传图片失败", e);
        }
        return null;
    }


    private static String uploadToLocal(String prefix, String fileName, InputStream fileInputStream) {

        try {
            String etc = null;
            if (StringUtils.isNotBlank(fileName)) {
                etc = StringUtils.substringAfterLast(fileName, ".").toLowerCase();
            }
            String name = UUID.randomUUID().toString() + "." + etc;
            File dir = new File(Paths.get(imgDir, prefix).toString());
            if (dir.exists() || dir.mkdirs()) {
                Path path = Paths.get(imgDir, prefix, name);
                File imgFile = new File(path.toUri());
                if (imgFile.exists() || imgFile.createNewFile()) {
                    FileOutputStream output = new FileOutputStream(imgFile);
                    StreamUtils.copy(fileInputStream, output);
                    output.flush();
                    output.close();
                }
                return StringUtils.replace(path.toString(), imgDir, "", 1);
            } else {
                throw new RuntimeException("文件存储位置获取失败，请联系服务器管理员");
            }
        } catch (Exception e) {
            LogUtil.err("上传图片失败", e);
        }
        return null;
    }

    private static String uploadToOss(String prefix, String fileName, String fileContentType, long fileSize, InputStream fileInputStream) {
        OSSClient client = new OSSClient(StringUtils.isBlank(endpointInternal) ? endpoint : endpointInternal, accessKeyId, secretAccessKey);
        String etc = null;
        if (StringUtils.isNotBlank(fileName)) {
            etc = StringUtils.substringAfterLast(fileName, ".").toLowerCase();
        }
        String name = UUID.randomUUID().toString() + "." + etc;
        String key = connectKey(prefix, name).toLowerCase();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(fileContentType);
        metadata.setContentLength(fileSize);
        PutObjectRequest request = new PutObjectRequest(bucketName, key, fileInputStream, metadata);
        PutObjectResult result = client.putObject(request);
        if (StringUtils.isNotEmpty(result.getETag())) {
            return key;
        }
        return null;
    }

    private static String uploadToOss(String prefix, MultipartFile file) {

        try {
            if (file.getSize() == 0) {
                LogUtil.err(prefix + "上传图片为空");
                return null;
            }
            return uploadToOss(prefix, file.getOriginalFilename(), file.getContentType(), file.getSize(), file.getInputStream());
        } catch (Exception e) {
            LogUtil.err("上传图片失败", e);
        }
        return null;
    }

    public static void deleteOSSPic(String key) {
        if (!isLocal) {
            try {
                key = key.replaceFirst(String.format("(?:http:)?//%s\\.%s/", bucketName, endpoint), "");
                OSSClient client = new OSSClient(endpoint, accessKeyId, secretAccessKey);
                client.deleteObject(bucketName, key);
            } catch (Exception e) {
                LogUtil.err("删除图片失败", e);
            }
        }
    }

    private static String getImageFormat(InputStream inputStream) throws IOException {
        String formatName = "";
        ImageInputStream stream = ImageIO.createImageInputStream(inputStream);
        Iterator<ImageReader> iterator = ImageIO.getImageReaders(stream);
        if (iterator.hasNext()) {
            formatName = iterator.next().getFormatName();
        }
        return formatName;
    }

    private static String getContentType(String fileNameExtension) {
        if (StringUtils.isBlank(fileNameExtension)) {
            return null;
        }
        String contentType = null;
        switch (fileNameExtension.toLowerCase()) {
            case "jpeg":
            case "jpg":
            case "jpe":
            case "jfif":
                contentType = "image/jpeg";
                break;
            case "png":
                contentType = "image/png";
                break;
            case "bmp":
                contentType = "image/bmp";
                break;
            case "gif":
                contentType = "image/gif";
                break;
            case "tif":
            case "tiff":
                contentType = "image/tiff";
                break;
            case "ico":
                contentType = "image/x-icon";
                break;
        }
        return contentType;
    }

    /**
     * 上传图片，根据图片http网络路径，先下载再上传
     */
    public static String uploadImageByUrl(String imageUrl) {
        if (StringUtils.isBlank(imageUrl)) {
            return null;
        }
        try {
            String https = "https://", http = "http://";
            if (!imageUrl.startsWith(http) && !imageUrl.startsWith(https)) {
                imageUrl = http + imageUrl;
            }
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5 * 1000);
            int status = connection.getResponseCode();
            if (status == HttpsURLConnection.HTTP_MOVED_TEMP || status == HttpsURLConnection.HTTP_MOVED_PERM || status == HttpsURLConnection.HTTP_SEE_OTHER) {
                url = new URL(connection.getHeaderField("Location"));
                connection = (HttpURLConnection) url.openConnection();
            }
            InputStream inputStream = connection.getInputStream();
            String prefix = "asset";
            if (connection.getContentLength() == 0) {
                return null;
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
            outputStream.close();
            InputStream tm = new ByteArrayInputStream(outputStream.toByteArray());
            InputStream im = new ByteArrayInputStream(outputStream.toByteArray());
            String etc = getImageFormat(tm);
            tm.close();
            if (StringUtils.isBlank(etc)) {
                LogUtil.err("试图上传一个非图片文件，上传失败");
                return null;
            }
            String s = UUID.randomUUID().toString() + "." + etc;
            String urlResult = "";
            if (!isLocal) {
                String contentType = getContentType(etc);
                if (StringUtils.isBlank(contentType)) {
                    return null;
                }
                urlResult = uploadToOss(prefix, s, contentType, connection.getContentLength(), im);
            } else {
                urlResult = uploadToLocal(prefix, s, im);
            }
            im.close();
            if (StringUtils.isNotBlank(urlResult)) {
                urlResult = getFullUrl(urlResult);
            }
            return urlResult;

        } catch (IOException e) {
            LogUtil.err("上传图片失败", e);
        }
        return null;
    }

    private static String upByContent(String content, String prefix) {
        Pattern compile = Pattern.compile("(data:([^;]+);base64,([a-zA-Z0-9+/=]))");
        Matcher matcher = compile.matcher(content);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String contentType = matcher.group(2);
            String base64Content = matcher.group(3);
            String fileName = contentType.replace("/", ".");
            //未完待续
        }
        return null;
    }

}
