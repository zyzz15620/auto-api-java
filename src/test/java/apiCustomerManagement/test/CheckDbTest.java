package apiCustomerManagement.test;

import apiCustomerManagement.model.user.dto.DbAddress;
import apiCustomerManagement.model.user.dto.DbUser;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.Test;

public class CheckDbTest {
    @Test
    void checkDatabaseConnection(){
        // A SessionFactory is set up once for an application!
        final StandardServiceRegistry registry =
                new StandardServiceRegistryBuilder()
                        .build();
        try {
            SessionFactory sessionFactory =
                    new MetadataSources(registry)
                            .addAnnotatedClass(DbUser.class)
                            .addAnnotatedClass(DbAddress.class)
                            .buildMetadata()
                            .buildSessionFactory();

            //Test thử cái vừa khởi tạo
            sessionFactory.inTransaction(session -> {
                session.createSelectionQuery("from DbAddress", DbAddress.class)
                        .getResultList()
                        .forEach(address->
                                System.out.println(address.getId()));
            });
        }
        catch (Exception e) {
            // The registry would be destroyed by the SessionFactory, but we
            // had trouble building the SessionFactory so destroy it manually.
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
