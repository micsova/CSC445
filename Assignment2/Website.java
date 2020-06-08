/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

//https://jsoup.org/apidocs/

package Assignment2;

import org.jsoup.*;
import org.jsoup.nodes.*;

import java.io.IOException;
import java.net.URL;


/**
 *
 * @author micsova
 */
public class Website {
    
    private String url;
    private String title;
    private Document doc;
    
    public Website(String u) throws IOException, IllegalArgumentException {
        try {
            Connection connection = Jsoup.connect(u); //Could add a USER_AGENT
            url = u;
            doc = connection.get();
            title = doc.title();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(u);
        }
    }
    
    public String retrieve() {
        if(doc.hasText()){
            return this.concat().toLowerCase();
        } else {
            return null;
        }
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getURL() {
        return url;
    }

    public Document getDoc() {
        return doc;
    }
    
    private String concat() {
        String ret;
        StringBuilder sb = new StringBuilder();
        for(Element element : doc.getAllElements()) {
            sb.append(element.ownText());
        }
        ret = sb.append(doc.text()).toString();
        return ret;
    }

    public static String error() {
        return "The website you entered does not exist";
    }
}
