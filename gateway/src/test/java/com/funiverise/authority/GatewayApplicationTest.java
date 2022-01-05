package com.funiverise.authority;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
@EnableScheduling
public class GatewayApplicationTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    @Test
    public void atomicNumFunctionTest() {
        AtomicLong num = new AtomicLong(10);
        num.addAndGet(-5);
        System.out.println(num);
    }

    @Test
    @Scheduled
    public void testTimeFormatter() {
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    }

    @Autowired
    private SessionFactory sessionFactory;
    @Test
    public void testAnonymousFun() {
        Session session = sessionFactory.openSession();
        session.doWork(connection -> connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED));


    }
}
