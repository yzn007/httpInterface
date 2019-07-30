package com.springboot.httpInterface.services;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.dom.DOMDocument;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import javax.lang.model.util.Elements;
import javax.xml.crypto.dsig.XMLObject;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by yzn00 on 2019/7/23.
 */
public class CxfWebService {

    public static void main(String []args){
        cl2();
    }

    public static void cl2() {
        // 创建动态客户端
        JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
        Client client = dcf.createClient("http://219.153.5.18:9081/net-ws/ws/NetWsServiceWrap?wsdl");
        // 需要密码的情况需要加上用户名和密码
        // client.getOutInterceptors().add(new ClientLoginInterceptor(USER_NAME,
        // PASS_WORD));
        Object[] objects = new Object[0];
        try {
            // 系统类别,接口Id,接口授权码,终端用户代号,终端用户姓名,终端标识,终端电子坐标,接口XML对象解密、解密KEY,数据操作对象;
            String terminalCode = "1234";
            String terminalNm = "test";
            String terminalId = "222.178.231.71";
            String terminalXY = "192,234";
            String enKey = "DfeDFEFDxXUF&*%*##*(#$JJFK";
            Document document = DocumentHelper.createDocument();
            DateFormat df = new SimpleDateFormat("yyyy-mm-dd");

            Element root = document.addElement("wrObjects");

            for(int i=0;i<1;i++){
                Element wrObject = root.addElement("wrObject");
                Element hpzl = wrObject.addElement("hpzl");
                hpzl.setText("02");
                Element hphm = wrObject.addElement("hphm");
                hphm.setText("渝ABCD12");
                Element txrqq = wrObject.addElement("txrqq");
                txrqq.setText(df.format(new Date()));
                Element txrqz = wrObject.addElement("txrqz");
                txrqz.setText(df.format(new Date()));
                Element txfw = wrObject.addElement("txfw");
                txfw.setText("1");
                Element txyy = wrObject.addElement("txyy");
                txyy.setText("测试");
                Element bz = wrObject.addElement("bz");
                bz.setText("测试写入");
                Element czlx = wrObject.addElement("czlx");
                czlx.setText("1");
            }

            StringWriter sw = new StringWriter();
            OutputFormat opf = OutputFormat.createPrettyPrint();
            opf.setEncoding("UTF-8");
            XMLWriter xmlWriter = new XMLWriter(sw,opf);
            try{
                xmlWriter.write(document);
            }catch (Exception e){

            }
            String xmlString = sw.toString();
            int k = 0;
           while(true) {
               String code = "";
               String msg = "";
               objects = client.invoke("writeObject", "39", "39W01", "21849186864529442191419884887906", terminalCode, terminalNm, terminalId, terminalXY, enKey, xmlString);
//               System.out.println("返回数据:" + objects[0]);
               Document retDoc = DocumentHelper.parseText(objects[0].toString());
               root = retDoc.getRootElement();
               for (Iterator iterator = root.elementIterator(); iterator.hasNext(); ) {
                   Element ele = (Element) iterator.next();
                   if (ele.getName().equalsIgnoreCase("head")) {
                       List<Element> els = ele.elements();

                       for (Element s : els) {
                           if (s.getName().equals("code")) {
                               code = s.getText();
                           }
                           if(s.getName().equalsIgnoreCase("message"))
                               msg = s.getText();
                       }
                       System.out.println("调用webservice返回code:"+code+"["+ URLDecoder.decode(msg)+"]");
                   }
               }
               if(code.equalsIgnoreCase("1"))
                   break;
               Thread.sleep(1000);
               if(k++>50)
                   break;
           }
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }

}
