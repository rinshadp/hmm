import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import javax.xml.bind.JAXBException;
import org.xml.sax.SAXException;

public class Parser {
    File f;
    Scanner in;
    List<Word> words;
    List<Label> tags;
    List<Trans> trans;
    String pre;
    
    Parser() {
        
        tags = new LinkedList<Label>();
        words = new LinkedList<Word>();
        trans = new LinkedList<Trans>();
        
        pre="."; 
        Label l = new Label(".");
        l.incCount();
        tags.add(l);
        
        Trans t =new Trans(".");
        trans.add(t);
        
        
    }
    
    void parseSentance(String S) throws SAXException, JAXBException, FileNotFoundException{
        f = new File(S);
        in = new Scanner(f);
        
        while(in.hasNext()){
            String s = in.next();
            String item[] = s.split("/");
            //for(String w:item){
            //System.out.println(w);  }
     
            //adding tags to take total tag count
                Iterator<Label> i = tags.listIterator();
                int tc=0;
                while(i.hasNext()){
                    Label l = i.next();
                    if(l.label.equals(item[1])){
                        tc = 1;
                        l.incCount();
                        break;
                    }
                }
                
                //if not  fount
                if(tc==0){
                Label temp = new Label(item[1]);
                temp.incCount();
                this.tags.add(temp);
                }
                
            //for transition probability
            Iterator<Trans> j = trans.listIterator();
            int ct=0;
            while(j.hasNext()){
                Trans t = j.next();
                if(t.label.equals(pre)){
                    ct=1;
                    Iterator<Label> it = t.follows.listIterator();
                    int cc=0;
                    while(it.hasNext()){
                        Label l = it.next();
                        if(l.label.equals(item[1])){
                            cc=1;
                            l.incCount();
                            this.pre=item[1];
                            break;
                        }
                    }
                    if(cc==0){
                        Label l = new Label(item[1]);
                        t.follows.add(l);
                        this.pre=item[1];
                    }
                }
            }
            //if first label not fount
            if(ct==0){
                Trans t= new Trans(pre);
                Label l = new Label(item[1]);
                l.incCount();
                this.pre=item[1];
                //System.out.println(pre + "-->" + item[1]);
                
                t.follows.add(l);
                trans.add(t);
            }
            
             
            //taking word count
             //   System.out.println("ff");
            int cw = 0;
            Iterator<Word> li = words.listIterator();
            while(li.hasNext()){
                    Word w;
                    w = li.next();
                    if(w.word.equals(item[0])){
                        cw = 1;
                        w.wordCount++;
                        //System.out.println(w.word);
                        Iterator<Label> tagIterator = w.labels.listIterator();
                        //searching for tag
                        int c=0;
                        while(tagIterator.hasNext()){
                            Label tag = tagIterator.next();
                            
                            if(tag.label.equals(item[1])){
                                c=1;
                                tag.incCount();
                                break;
                            }  
                        }
                        //if no tag found add tag
                            if(c==0){
                                Label l = new Label(item[1]);
                                w.tagCount++;
                                //System.out.println(w.word);
                                w.labels.add(l);
                            }
                        break;
                    }   
                }
            
                if(cw == 0){
                    Word temp = new Word(item[0]);
                    Label l = new Label(item[1]);
                    l.incCount();
                    temp.labels.add(l);
                    words.add(temp);
        
            }
        
        }
        //System.out.println(words.get(1).word);
        
    }
    
    List<Word> makeProbability(){
        Iterator<Word> iw = words.listIterator();
        while(iw.hasNext()){
            Word w = iw.next();
            //System.out.print(w.word);
            Iterator<Label> il = w.labels.listIterator();
            while(il.hasNext()){
                Label l = il.next();
                Iterator<Label> it = tags.listIterator();
                while(it.hasNext()){
                    Label x = it.next();
                    if(l.label.equals(x.label)){
                        l.prob = (double) l.count/x.count;
                        //System.out.print(l.label + "--->" + l.prob);
                        //if(l.prob>1)
                        //    System.out.println("eror");
                        break;
                    }
                }
            }
            //System.out.println("");
        }
        
        Iterator<Trans> i = trans.listIterator();
        while(i.hasNext()){
            Trans t = i.next();
            Iterator<Label> it = tags.listIterator();
            int count=0;
            while(it.hasNext()){
                Label l = it.next();
                if(t.label.equals(l.label)){
                    count = l.count;
                    break;
                }
            }
            it = t.follows.listIterator();
            while(it.hasNext()){
                Label l = it.next();
                l.prob=(double) l.count/count;
               // if(l.prob>1)
                 //   System.out.println("error");
            }
        }
        return words;
    }

    double[][] getTransMatrix() {
        double[][] prob = new double[tags.size()][tags.size()];
        for(int i=0;i<tags.size();i++){
            String si=tags.get(i).label;
            for(int j=0;j<tags.size();j++){
                String sj = tags.get(j).label;
                Iterator<Trans> it = trans.listIterator();
                Trans t=null;
                while(it.hasNext()){
                    t=it.next();
                    if(t.label.equals(si))
                        break;
                }
                Iterator<Label> il;
                il = t.follows.listIterator();
                prob[i][j]=0;
                while(il.hasNext()){
                    Label l = il.next();
                    if(l.label.equals(sj))
                    {
                        prob[i][j]=Math.log(l.prob);
                        break;
                    }
                }
                //System.out.print(prob[i][j]+"  ");
            }
           // System.out.println("");
        }
        
        return prob;
    }
    
    
}

class Word{
    String word;
    List<Label> labels;
    int tagCount,wordCount;
    
    Word(String word){
       this.labels = new LinkedList<Label>();
       this.word= word;
       this.tagCount=1;
       this.wordCount=1;
    }
    
}

class Label{
    String label;
    int count;
    double prob;
    Label(String label){
        this.label = label;
        count = 0;
    }
    
    void incCount(){
        this.count++;
    }
}

class Trans{
    String label;
    List<Label> follows;
    
    Trans(String s){
        this.label=s;
        follows = new LinkedList<Label>();
        
    }
}
