package t;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by guanyf on 2016/11/14.
 */
public class SQLTest {
    public static void main(String[] args) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
        String url="jdbc:oracle:thin:@localhost:1521:xe";
        String user="idmm";
        String password="ll";
        Connection conn=DriverManager.getConnection(url,user,password);
        Connection co = DriverManager.getConnection(url, user, password);

        co.close();
    }
}
