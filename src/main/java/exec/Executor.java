package exec;

import okhttp3.*;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Executor {
    private static final Logger log = LoggerFactory.getLogger(Executor.class);
    private static final List<Area> AREA_LIST = new ArrayList<>();
    private static OkHttpClient HTTP_CLIENT = new OkHttpClient();
    private static int index;

    static {
        AREA_LIST.add(new Area("11", "北京市", "北京市", null, false));
        AREA_LIST.add(new Area("12", "天津市", "天津市", null, false));
        AREA_LIST.add(new Area("13", "河北省", "河北省", null, false));
        AREA_LIST.add(new Area("14", "山西省", "山西省", null, false));
        AREA_LIST.add(new Area("15", "内蒙古自治区", "内蒙古自治区", null, false));
        AREA_LIST.add(new Area("21", "辽宁省", "辽宁省", null, false));
        AREA_LIST.add(new Area("22", "吉林省", "吉林省", null, false));
        AREA_LIST.add(new Area("23", "黑龙江省", "黑龙江省", null, false));
        AREA_LIST.add(new Area("31", "上海市", "上海市", null, false));
        AREA_LIST.add(new Area("32", "江苏省", "江苏省", null, false));
        AREA_LIST.add(new Area("33", "浙江省", "浙江省", null, false));
        AREA_LIST.add(new Area("34", "安徽省", "安徽省", null, false));
        AREA_LIST.add(new Area("35", "福建省", "福建省", null, false));
        AREA_LIST.add(new Area("36", "江西省", "江西省", null, false));
        AREA_LIST.add(new Area("37", "山东省", "山东省", null, false));
        AREA_LIST.add(new Area("41", "河南省", "河南省", null, false));
        AREA_LIST.add(new Area("42", "湖北省", "湖北省", null, false));
        AREA_LIST.add(new Area("43", "湖南省", "湖南省", null, false));
        AREA_LIST.add(new Area("44", "广东省", "广东省", null, false));
        AREA_LIST.add(new Area("45", "广西壮族自治区", "广西壮族自治区", null, false));
        AREA_LIST.add(new Area("46", "海南省", "海南省", null, false));
        AREA_LIST.add(new Area("50", "重庆市", "重庆市", null, false));
        AREA_LIST.add(new Area("51", "四川省", "四川省", null, false));
        AREA_LIST.add(new Area("52", "贵州省", "贵州省", null, false));
        AREA_LIST.add(new Area("53", "云南省", "云南省", null, false));
        AREA_LIST.add(new Area("54", "西藏自治区", "西藏自治区", null, false));
        AREA_LIST.add(new Area("61", "陕西省", "陕西省", null, false));
        AREA_LIST.add(new Area("62", "甘肃省", "甘肃省", null, false));
        AREA_LIST.add(new Area("63", "青海省", "青海省", null, false));
        AREA_LIST.add(new Area("64", "宁夏回族自治区", "宁夏回族自治区", null, false));
        AREA_LIST.add(new Area("65", "新疆维吾尔自治区", "新疆维吾尔自治区", null, false));
    }

    public static void main(String[] args) {
        for (int i = 0; i < AREA_LIST.size(); i++) {
            resolveAll(AREA_LIST.get(i));
            index = i;
        }

    }

    private static int resolvedCount = 0;

    private static void resolveAll(Area parentArea) {
        List<Area> areas = resolve(parentArea);
        areas.forEach(area -> {
            if (area.name.contains(",")) {
                log.error("执行出错: ", new RuntimeException("不支持的区域名称（有英文逗号） " + area.name));
                throw new RuntimeException("不支持的区域名称（有英文逗号） " + area.name);
            }
        });
        areas.forEach(area -> {
            if (!area.leaf) {
                resolveAll(area);
            }
        });
    }

    private static List<Area> resolve(Area parentArea) {
        while (true) {
            try {
                return resolveInternal(parentArea);
            } catch (SocketTimeoutException ex) {
                HTTP_CLIENT = new OkHttpClient().newBuilder()
                        .followRedirects(false) //禁制OkHttp的重定向操作，我们自己处理重定向
                        .followSslRedirects(false)
                        .cookieJar(new LocalCookieJar())  //为OkHttp设置自动携带Cookie的功能
                        .build();
            } catch (IOException ex) {
                log.error("执行出错: ", ex);
                throw new RuntimeException(ex);
            }
        }
    }

    private static List<Area> resolveInternal(Area parentArea) throws IOException {
        try {
            Thread.sleep(1000l);
        } catch (InterruptedException e) {
            log.error("执行出错: ", e);
            e.printStackTrace();
        }

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2018");

        String parentCode = parentArea.code;

        int codeLen = parentCode.length();
        if (codeLen > 2) urlBuilder.append('/').append(parentCode, 0, 2);
        if (codeLen > 4) urlBuilder.append('/').append(parentCode, 2, 4);
        if (codeLen > 6) urlBuilder.append('/').append(parentCode, 4, 6);
        urlBuilder.append('/').append(parentCode).append(".html");

        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(urlBuilder.toString().replaceAll("/00/", "/"));
        requestBuilder.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0");
        requestBuilder.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        requestBuilder.header("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
        requestBuilder.header("Cookie", "_trs_uv=jyr1gamz_6_ciri; __utmz=207252561.1565088893.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); __utma=207252561.1624264542.1565088893.1565140506.1565142776.5; AD_RS_COOKIE=20082855");
        requestBuilder.cacheControl(CacheControl.FORCE_NETWORK);
        try (Response response = HTTP_CLIENT.newCall(requestBuilder.build()).execute()) {
            if (response == null || response.code() != 200 || response.body() == null) {
                log.error("执行出错: ", new RuntimeException("Error response " + response));
                throw new RuntimeException("Error response " + response);
            }

            byte[] bs = response.body().bytes();
            FileUtils.writeByteArrayToFile(new File("/home/site/" + urlBuilder.toString().replaceAll("/00/", "/").replaceAll("http://www.stats.gov.cn", "")), bs);
//            FileUtils.writeByteArrayToFile(new File("d:/site/" + urlBuilder.toString().replaceAll("/00/", "/").replaceAll("http://www.stats.gov.cn", "")), bs);
            Document document = Jsoup.parse(new String(bs, "GBK"));
            Elements elements = document.getElementsByClass("citytable");
            if (elements.size() != 1) {
                elements = document.getElementsByClass("countytable");
                if (elements.size() != 1) {
                    elements = document.getElementsByClass("towntable");
                    if (elements.size() != 1) {
                        elements = document.getElementsByClass("villagetable");
                        if (elements.size() != 1) {
                            log.error("执行出错: ", new RuntimeException());
                            throw new RuntimeException();
                        }
                    }
                }
            }

            elements = elements.get(0).child(0).children();
            List<Area> areas = new ArrayList<>(elements.size());
            for (Element element : elements) {
                String className = element.className();
                if ("tr".equalsIgnoreCase(element.nodeName()) &&
                        ("citytr".equalsIgnoreCase(className)
                                || "countytr".equalsIgnoreCase(className)
                                || "towntr".equalsIgnoreCase(className)
                                || "villagetr".equalsIgnoreCase(className))) {
                    Elements tdElements = element.children();

                    int tdElementsSize = tdElements.size();
                    if (tdElementsSize != 2 && tdElementsSize != 3) {
                        log.error("执行出错: ", new RuntimeException());
                        throw new RuntimeException();
                    }
                    String areaId, areaName;
                    boolean leaf = true;
                    Element areaIdElement = tdElements.get(0);
                    Element areaNameElement = tdElements.get(tdElementsSize - 1);
                    if (areaIdElement.children().size() == 1 && "a".equalsIgnoreCase(areaIdElement.child(0).nodeName())) {
                        areaId = areaIdElement.child(0).text();
                        areaName = areaNameElement.child(0).text();
                        leaf = false;
                    } else if (areaIdElement.children().size() == 0) {
                        areaId = areaIdElement.text();
                        areaName = areaNameElement.text();
                    } else {
                        log.error("执行出错: ", new RuntimeException());
                        throw new RuntimeException();
                    }

                    log.info(" 爬取: {}--{}--{}", parentArea.fullName.replaceAll("\\.", "--"), areaName, areaId);
//                    log.error(" 测试发送邮箱 ", "成功收到！");
                    areas.add(new Area(getShortAreaId(areaId), areaName, parentArea.fullName + '.' + areaName, parentCode, leaf));
                }
            }
            return areas;
        }
    }

    private static String getShortAreaId(String areaId) {
        if (areaId.length() != 12) {
            throw new RuntimeException(areaId);
        }
        if (areaId.endsWith("0000000000")) {
            return areaId.substring(0, 2);
        } else if (areaId.endsWith("00000000")) {
            return areaId.substring(0, 4);
        } else if (areaId.endsWith("000000")) {
            return areaId.substring(0, 6);
        } else if (areaId.endsWith("000")) {
            return areaId.substring(0, 9);
        } else {
            return areaId;
        }
    }

    private static class Area {
        private String code;
        private String name;
        private String fullName;
        private String parent;
        private boolean leaf;

        public Area(String code, String name, String fullName, String parent, boolean leaf) {
            this.code = code;
            this.name = name;
            this.fullName = fullName;
            this.parent = parent;
            this.leaf = leaf;
        }
    }

    //CookieJar是用于保存Cookie的
    private static class LocalCookieJar implements CookieJar {
        List<Cookie> cookies;

        @Override
        public List<Cookie> loadForRequest(HttpUrl arg0) {
            if (cookies != null)
                return cookies;
            return new ArrayList<Cookie>();
        }

        @Override
        public void saveFromResponse(HttpUrl arg0, List<Cookie> cookies) {
            this.cookies = cookies;
        }

    }
}

