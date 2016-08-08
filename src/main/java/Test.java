import org.neo4j.driver.v1.*;

/**
 * Created by brianzhao on 8/7/16.
 */
public class Test {
    public static void main(String[] args) {
        Driver driver = GraphDatabase.driver("bolt://192.168.99.100", AuthTokens.basic("neo4j", "neo"));
        Session session = driver.session();


        session.run("CREATE (:Person {name:'Arthur', title:'King'}) -[:LIKE]-> (:Person {name:'Brian'})");


//        StatementResult result = session.run("MATCH (a:Person) WHERE a.name = 'Arthur' RETURN a.name AS name, a.title AS title");
//        while (result.hasNext()) {
//            Record record = result.next();
//            System.out.println(record.get("title").asString() + " " + record.get("name").asString());
//        }

        session.close();
        driver.close();
    }
}
