import java.util.Properties;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.boomi.document.scripting.DataContextImpl;
import com.boomi.execution.ExecutionUtil;  
import java.util.logging.Logger;  

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class Line 
{
    public Integer lineNo = 0;
    public List<Field> fields = new ArrayList<Field>();
    
    public Line(Integer lineNo)
    {
        this.lineNo = lineNo;
    }
    
    public void addField(String name, String value)
    {
        fields.add(new Field(name, value));
    }
}

public class Field
{
    public String name;
    public String value;
    
    public Field(){}
    
    public Field(String name, String value)
    {
        this.name = name;
        this.value = value;
    }
}

public class CSVToXML
{
    

    public void convert(DataContextImpl dataContext)
    {
        String classname = ExecutionUtil.getDynamicProcessProperty("DatabaseDriverClass");
        String user = ExecutionUtil.getDynamicProcessProperty("UserName");
        
        for( int i = 0; i < dataContext.getDataCount(); i++ ) 
        {
            InputStream is = dataContext.getStream(i);
            Properties props = dataContext.getProperties(i);
            Integer lineNo = 0;
            Map<Integer,String> columns = new HashMap<Integer,String>();
            List<Line> lines = new ArrayList<Line>();
            
            //is will contain multi rows, the headers and then rows of data
            BufferedReader reader=new BufferedReader(new InputStreamReader(is))

            //read file one row at a time 
            while(reader.ready())
            {
                lineNo++;
                String line = reader.readLine();
                
                if(line.trim().length() > 0)
                {
                    String[] bits = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*\$)", -1);
                    
                    //if line 0 then add to cols 
                    if(lineNo==1)
                    {
                        for(int pos = 0 ; pos < bits.size() ; pos++)
                        {
                            columns.put(pos, bits[pos].trim());
                        }
                    }
                    else
                    {
                        Line newline = new Line(lineNo);
                        
                        for(int pos = 0 ; pos < bits.size() ; pos++)
                        {
                            newline.addField(columns.get(pos), bits[pos].trim().replaceAll("'",""));
                        }
                        
                        lines.add(newline);
                    }
                }
            }
            
            //push each line to a document
            for(Line line : lines)
            {
                returnDocumentNamedTags(dataContext, line);
            }

        }
    }
    
    public void returnDocumentNamedTags(DataContextImpl dataContext, Line line)
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
   	    DocumentBuilder builder = factory.newDocumentBuilder();
       	Document doc = builder.newDocument();
           	
       	Element nRoot = null;
       	Element nFields = null;
       	Element nField = null;
       	Element nName = null;
       	Element nValue = null;
       	Text t = null;
       	
       	//add Root 
       	nRoot = doc.createElement("Record");
       	doc.appendChild(nRoot);
       	

        for(Field field : line.fields)
        {

            nName = doc.createElement(toCamelCase(field.name));
            nName.appendChild(doc.createTextNode(field.value));
      
       	    nRoot.appendChild(nName);
        }
        
        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);
        
        Properties docProps = dataContext.getProperties(0);
        dataContext.storeStream(new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8)),docProps);
        
    }
    
    public void returnDocumentGenericTags(DataContextImpl dataContext, Line line)
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
   	    DocumentBuilder builder = factory.newDocumentBuilder();
       	Document doc = builder.newDocument();
           	
       	Element nRoot = null;
       	Element nFields = null;
       	Element nField = null;
       	Element nName = null;
       	Element nValue = null;
       	Text t = null;
       	
       	//add Root 
       	nRoot = doc.createElement("Record");
       	doc.appendChild(nRoot);
       	
       	nFields = doc.createElement("Fields");
       	nRoot.appendChild(nFields);
       	
       	
       	for(Field field : line.fields)
        {
            nField = doc.createElement("Field");
            
            nName = doc.createElement("Name");
            nName.appendChild(doc.createTextNode(field.name));
            nField.appendChild(nName);
            
            nValue = doc.createElement("Value");
            nValue.appendChild(doc.createTextNode(field.value));
            nField.appendChild(nValue);
            
       	    nFields.appendChild(nField);
           
        }
        
        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);
        
        Properties docProps = dataContext.getProperties(0);
        dataContext.storeStream(new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8)),docProps);
        
    }
    
    public String toCamelCase(String s) 
    {

        s = s.replaceAll("`|'", "");
        
        String[] tokens = s.split("[\\W_]+|(?<=[a-z])(?=[A-Z][a-z])"); 
        
        s = "";
        
        for (String token : tokens) 
        {
            String lowercaseToken = token.toLowerCase();
            
            if(tokens[0].equals(token))
            {
                s += lowercaseToken;
            }
            else
            {
                s += lowercaseToken.toUpperCase().charAt(0);
                s += lowercaseToken.substring(1);
            }
        }
        
        return s;
    }
        
    
}

CSVToXML convertor = new CSVToXML();
convertor.convert(dataContext);


