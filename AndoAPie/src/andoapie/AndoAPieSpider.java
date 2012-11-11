/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package andoapie;

/**
 *
 * @author Yun
 */

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AndoAPieSpider {
    public static void main(String[] args) throws MalformedURLException, Exception {
        AndoAPieSpider app=new AndoAPieSpider();
        
        String url="http://www.rutasjalisco.gob.mx/rutas-por-empresas-de-transporte/red-parapanamericana/";
        int arg1=0;
        int arg2=0;
        int arg3=0;
        
        String[] fl=url.split("/");
        File file=new File("urls_"+fl[fl.length-1]+".txt");
        System.out.print("Buscando archivo "+file.getAbsolutePath()+" ... ");
        if(file.exists()){
            System.out.println("Econtrado!");
            FileInputStream stream = new FileInputStream(file);
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            app.getKMLs(Charset.defaultCharset().decode(bb).toString().split("\n"));
            stream.close();
        }else{
            System.out.println("no se encontro, obteniendo urls!");
            app.runAll(url,arg1,arg2,arg3,file.getAbsolutePath());
        }
    }

    private void runAll(String arg1,int arg2,int arg3, int arg4, String fname) throws MalformedURLException, Exception {
        System.out.println("\nObteniendo lista de archivos html ------------------------");
        System.out.print("Obteniendo-> "+arg1+" ... ");
        WebFile file= new WebFile(arg1);
        Object content = file.getContent( );
        System.out.println("DONE!");
        if ( content instanceof String ){
            String html = (String)content;
            html=html.substring(html.indexOf("<td class=\"sectiontableheader\" align=\"right\" width=\"5%\">"),html.indexOf("<td align=\"center\" colspan=\"4\" class=\"sectiontablefooter\">"));
            System.out.println("\nObteniendo URLs Base ------------------------");
            html = getBaseURLs(arg1,html);
            if(arg2!=0 && arg3!=0){
                for(int i=arg2;i<=arg3;i++){
                    System.out.print(arg1+"Pagina-"+i+"-"+arg4+".html ... ");
                    file= new WebFile(arg1+"Pagina-"+i+"-"+arg4+".html");
                    content = file.getContent( );
                    System.out.println("DONE!");
                    String html2= (String)content;
                    html2=html2.substring(html2.indexOf("<td class=\"sectiontableheader\" align=\"right\" width=\"5%\">"),html2.indexOf("<td align=\"center\" colspan=\"4\" class=\"sectiontablefooter\">"));
                    html = html+getBaseURLs(arg1,html2);
                }
            }
            html=getKMLURLS(html.split("\n"));

            System.out.print("Guardando-> "+fname+" ... ");
            FileWriter fstream = new FileWriter(fname);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(html);
            out.close();
            System.out.print("Done!");

            getKMLs(html.split("\n"));
        }
    }
    
    private String getBaseURLs(String baseUrl, String html){
        StringBuilder sb=new StringBuilder("");
        String[] url=baseUrl.split("/");
        Pattern pattern = Pattern.compile("/"+url[3]+"/"+url[4]+"/[\\w\\-]+\\.html");
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            sb.append(html.substring(matcher.start(),matcher.end()));
            sb.append("\n");
        }
        return sb.toString();
    }
    
    private String getKMLURLS(String[] urls) throws MalformedURLException, Exception{
        System.out.println("\nObteniendo URLS de KML ------------------------");
        int n=urls.length;
        StringBuilder sb=new StringBuilder("");
        for(int i=0;i<n;i++){
            WebFile file;
            if(urls[i].equals("") || urls[i].isEmpty()){
                
            }else{
                file= new WebFile("http://www.rutasjalisco.gob.mx"+urls[i]);
                Object content = file.getContent( );
                if ( content instanceof String ){
                    String html = (String)content;
                    sb.append(getKMLURL(html));
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }
    
    private String getKMLURL(String url){
        StringBuilder sb=new StringBuilder();
        Pattern pattern = Pattern.compile("http://www.rutasjalisco.gob.mx/mapaskml/[\\w\\-_]+/[\\w\\-_]+\\.kml");
        Matcher matcher = pattern.matcher(url);
        while (matcher.find()) {
            System.out.print("Obteniendo-> "+matcher.group()+" ... ");
            if(url.substring(matcher.start(),matcher.end()).contains("--")){
                String correct=matcher.group();
                correct=correct.replace("--", "-");
                sb.append(correct);
            }else{
                sb.append(url.substring(matcher.start(),matcher.end()));
            }
            sb.append("\n");
            System.out.println("Done!");
        }
        return sb.toString();
    }
    /*
    public static void getKMLs(String[] urls) throws MalformedURLException, Exception{
        System.out.println("\nObteniendo KMLs ------------------------");
        int n=urls.length;
        for(int i=0;i<n;i++){
            WebFile file;
            if(urls[i].equals("") || urls[i].isEmpty()){
                
            }else{
                System.out.print("Obteniendo-> "+urls[i]+" ... ");
                file= new WebFile(urls[i]);
                Object content = file.getContent( );
                System.out.println("Done!");
                String kml;
                if ( content instanceof String ){
                    try{
                        String[] url=urls[i].split("//");
                        kml= (String)content;
                        System.out.print("Guardando-> "+url[1]+" ... ");
                        FileWriter fstream = new FileWriter(url[1]);
                        BufferedWriter out = new BufferedWriter(fstream);
                        out.write(kml);
                        out.close();
                        System.out.print("Done!");
                    }catch (Exception e){
                        System.err.println("Error: " + e.getMessage());
                    }
                }
            }
        }
    }
    */
    
    private void getKMLs(String[] urls) throws MalformedURLException, Exception{
        System.out.println("\nObteniendo KMLs ------------------------");
        BufferedInputStream in = null;
    	FileOutputStream fout = null;
        for(int i=0;i<urls.length;i++){
            if(!urls[i].isEmpty() && !urls[i].equals("")){
                try{
                    String[] file=urls[i].split("//");
                    System.out.print("Guardando-> "+file[1]+" ... ");
                    File f=new File(file[1]);
                    if(!f.getParentFile().exists()){
                        f.getParentFile().mkdirs();
                    }
                    in = new BufferedInputStream(new URL(urls[i]).openStream());
                    fout = new FileOutputStream(file[1]);

                    byte data[] = new byte[1024];
                    int count;
                    while ((count = in.read(data, 0, 1024)) != -1)
                    {
                        fout.write(data, 0, count);
                    }
                    System.out.println("Done!!!!");
                }finally{
                    if (in != null)
                        in.close();
                    if (fout != null)
                        fout.close();
                }
            }
        }
    }
}