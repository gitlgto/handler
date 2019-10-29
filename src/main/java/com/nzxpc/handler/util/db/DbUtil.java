package com.nzxpc.handler.util.db;

import com.nzxpc.handler.util.BeanContext;
import com.nzxpc.handler.mem.core.entity.Result;
import com.nzxpc.handler.util.LogUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.function.Supplier;

@SuppressWarnings("ALL")
public class DbUtil {
    private DbUtil() {
    }

    private static JdbcTemplate jt;

    public static void setJt(JdbcTemplate jdbcTemplate) {
        jt = jdbcTemplate;
    }

    public static <T> SqlHelper<T> getSqlHelper(Class<T> tClass) {
        return new MySqlHelper<>(tClass, jt);
    }

    /**
     * 处理事务，发生异常进行回滚
     *
     * @param platformTransactionManager
     * @param supplier
     * @return
     */
    private static Result doTransaction(PlatformTransactionManager platformTransactionManager, Supplier<Result> supplier) {
        TransactionStatus ts = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
        Result ret = new Result();
        try {
            ret = supplier.get();
            if (!ret.isOk()) {
                platformTransactionManager.rollback(ts);
            } else {
                platformTransactionManager.commit(ts);
            }
        } catch (Throwable e) {
            platformTransactionManager.rollback(ts);
            ret.setOk(false).setMsg("数据库操作失败");
            LogUtil.err(DbUtil.class, e);
            throw new RuntimeException(e);
        }
        return ret;
    }

    public Result transation(Supplier<Result> supplier) {
        return doTransaction(BeanContext.getBean(PlatformTransactionManager.class), supplier);
    }
}
