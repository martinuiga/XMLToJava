import static org.apache.commons.lang.StringUtils.replace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import ee.track.model.Data;
import ee.track.model.Data.DataBuilder;
import ee.track.service.ProcessorService;


public class XMLtoJava implements ProcessorService {
    private static Logger logger = Logger.getLogger(ProcessorServiceImpl.class);

    private String url;
    private List<Data> dataList;

    public ProcessorServiceImpl(String url) {
        this.url = url;
        this.dataList = new ArrayList<>();
    }

    @Override
    public List<Data> getData() {
        Document doc = getDocument();

        if (doc == null) {
            logger.warn("Can't get document from url: " + url);
            return dataList;
        }

        Elements elements = doc.select(".result-row");
        for (Element row : elements) {
            DataBuilder builder = new Data.DataBuilder();
            for (Node node : row.childNodes()) {
                for (Attribute attr : node.attributes()) {
                    if ("pictures".equals(attr.getValue())) {
                        for (Node n : node.childNodes()) {
                            if (n.hasAttr("class")) {
                                builder.setId("http://www.auto24.ee" + n.attr("href"));
                            }
                        }
                    } else if ("make_and_model".equals(attr.getValue())) {
                        for (Node n : node.childNodes()) {
                            for (Attribute attr2 : n.attributes()) {
                                if ("href".equals(attr2.getKey())) {
                                    String mark = ((Element) n).html();
                                    builder.setMark(mark);
                                } else if ("extra".equals(attr2.getValue())) {
                                    String description = ((Element) n).html();
                                    description = replace(description, "\n", "");
                                    description = replace(description, " <span>&nbsp;</span>", ", ");
                                    description = replace(description, "<span>&nbsp;</span>", ", ");
                                    description = replace(description, "<span></span>", ", ");
                                    description = replace(description, "&nbsp;", "");
                                    builder.setDescription(description);
                                }
                            }
                        }
                    } else if ("year".equals(attr.getValue())) {
                        builder.setYear(((Element) node).html());
                    } else if ("fuel".equals(attr.getValue())) {
                        builder.setFuel(((Element) node).html());
                    } else if ("transmission".equals(attr.getValue())) {
                        builder.setGear(((Element) node).html());
                    } else if ("price".equals(attr.getValue())) {
                        String price = ((Element) node).html();
                        price = replace(price, "<small>", "");
                        price = replace(price, "</small>", "");
                        price = replace(price, "&nbsp;", "");
                        builder.setPrice(price);
                    }
                }
            }
            builder.setName("");
            dataList.add(builder.build());
        }

        return dataList;
    }

    protected Document getDocument() {
        Document doc = null;
        try {
            doc = Jsoup.connect(url).timeout(10000).get();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return doc;
    }

}