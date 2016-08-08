import org.apache.commons.lang3.StringEscapeUtils;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by brianzhao on 8/7/16.
 */
public class IndexingLinks {
    private File linkData;
    private Driver driver;
    private String host;

    public IndexingLinks(File linkData, String host) {
        this.linkData = linkData;
        this.driver = GraphDatabase.driver("bolt://" + host, AuthTokens.basic("neo4j", "neo4j"));
    }

    public void processNutchLinks() {
        int numLines = 0;
        try {
            numLines = countLines(linkData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(linkData));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line;
        int lineNumber = 0;
        int numLinkStructuresProcessed = 0;

        String currentEndLink = null;
        List<LinkAnchor> startLinks = new ArrayList<>();
        boolean inLinkTextStructure = false;

        try {
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (!inLinkTextStructure) {
                    currentEndLink = line.split("\\s+")[0];
                    inLinkTextStructure = true;
                } else {
                    if (line.isEmpty()) {
                        //last line with current endlink
                        addEntities(startLinks, currentEndLink);
                        inLinkTextStructure = false;
                        startLinks.clear();
                        numLinkStructuresProcessed++;
                    } else {
                        String[] splitted = line.split("\\s+");
                        String startLink = splitted[1];
                        StringBuilder anchorText = new StringBuilder();
                        for (int i = 3; i < splitted.length; i++) {
                            anchorText.append(splitted[i]).append(' ');
                        }
                        String actualAnchorText = anchorText.toString().trim();
                        startLinks.add(new LinkAnchor(startLink, actualAnchorText));
                    }
                }
                lineNumber++;
                System.out.println(lineNumber + "\t/\t" + numLines);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        driver.close();
    }

    private int countLines(File file) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            is.close();
        }
    }

    private void addEntities(List<LinkAnchor> startLinks, String endLink) {
        //TODO fix this hacky query when I actually know more about NEO4J
        ensureLinkNodeExists(endLink);


        String query = "MATCH (start:LINK {url:\"%s\"})\n" +
                "CREATE UNIQUE (start)-[:LINKS_TO {anchor:\"%s\"}]->(:LINK {url:\"%s\"})";

        for (LinkAnchor linkAnchor : startLinks) {
            //ensure beginningLinkExists
            ensureLinkNodeExists(linkAnchor.getLink());
            ensureRelationShipExists(linkAnchor.getLink(), endLink, linkAnchor.getAnchor());
        }
    }

    /**
     * assumes startURL and endURL nodes already exists, or else may result in weird behavior
     *
     * @param startUrl
     * @param endUrl
     * @param anchorText
     */
    private void ensureRelationShipExists(String startUrl, String endUrl, String anchorText) {
        startUrl = StringEscapeUtils.escapeJava(startUrl);
        endUrl = StringEscapeUtils.escapeJava(endUrl);
        anchorText = StringEscapeUtils.escapeJava(anchorText);
        String relationshipQuery =
                "MATCH (start:LINK {url:\"%s\"})\n" +
                        "MATCH (end:LINK {url:\"%s\"})\n" +
                        "MERGE (start)-[:LINKS_TO {anchor:\"%s\"}]->(end)";
        Session session = driver.session();
        relationshipQuery = String.format(relationshipQuery, startUrl, endUrl, anchorText);
        session.run(relationshipQuery);
        session.close();
    }

    private void ensureLinkNodeExists(String url) {
        Session session = driver.session();
        String ensureEndLinkExists = "MERGE (:LINK {url:\"%s\"})";
        ensureEndLinkExists = String.format(ensureEndLinkExists,
                StringEscapeUtils.escapeJava(url));
        session.run(ensureEndLinkExists);
        session.close();
    }

    private class LinkAnchor {
        private String link;
        private String anchor;

        public LinkAnchor(String link, String anchor) {
            this.link = link;
            this.anchor = anchor;
        }

        public String getLink() {
            return link;
        }

        public String getAnchor() {
            return anchor;
        }
    }

    public static void main(String[] args) {
        String host = System.getenv("NEO4J_IP");
        IndexingLinks indexingLinks = new IndexingLinks(new File("link_data.txt"), host);
        indexingLinks.processNutchLinks();
    }
}
