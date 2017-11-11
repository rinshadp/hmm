/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nlp;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import javax.xml.bind.JAXBException;
import org.xml.sax.SAXException;
        
/**
 *
 * @author rinshad
 */
public class Nlp {
    
    double[][] transProb;
    List<Word> words;
    List<Label> tags;
    int numTags;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, SAXException, JAXBException {
        // TODO code application logic here
        Nlp nlp = new Nlp();
        
        Parser parser = new Parser();
        
        File folder = new File("brown");
        File[] fileList = folder.listFiles();
        
        for(int i=0;i<fileList.length;i++){
            if(fileList[i].isFile())
            {
                parser.parseSentance(fileList[i].getAbsolutePath());
                System.out.println(fileList[i].getAbsolutePath());
            }
        }
       /* 
        Iterator<Word> i = parser.words.listIterator();
        while(i.hasNext()){
            Word w = i.next();
            System.out.println(w.word + "-->" + w.wordCount);
        }
               
        //System.out.println(parser.tags.size());
        //System.out.println(parser.trans.size());*/
        
        nlp.words=parser.makeProbability();
        nlp.transProb = parser.getTransMatrix();
        nlp.tags=parser.tags;
        nlp.numTags=parser.tags.size();
        System.out.println(nlp.numTags);
        
       
        
        folder = new File("brown/test");
        fileList = folder.listFiles();
        long total=0,wordcount=0;
        List<String> symbols = new LinkedList<String>();
        List<String> tags = new LinkedList<String>();
        for(int i=0;i<fileList.length;i++){
           if(fileList[i].isFile()){
               Scanner in = new Scanner(new File(fileList[i].getAbsolutePath()));
                
                while(in.hasNext()){
                    String s=in.next();
                    String item[] = s.split("/");
                    symbols.add(item[0]);
                    tags.add(item[1]);
                    if(item[1].equals(".")){
                        wordcount=wordcount+symbols.size();
                        int a = nlp.viterbiFun(symbols.size(),symbols.toArray(new String[symbols.size()]) , tags.toArray(new String[tags.size()]));
                        // System.out.println(a);
                        total=total+a;
                        symbols.clear();
                        tags.clear();
                    }
          
                }
           }
       }
        System.out.println(total + "   " + wordcount);
    
    }
    
    int viterbiFun(int x,String[] symbols,String[] tagsSequence){
        int n=0;
        if(x==1)
            return 1;
        double[][] viterbi = new double[numTags+2][x];
        int [][] backPointer = new int[numTags+2][x];
        //System.out.print(x);    
        for(int s=0;s<numTags;s++){
            double xx= emitionProb(symbols[0], tags.get(s).label);
            //System.out.println(xx);
            viterbi[s][0]=xx;
                
            backPointer[s][0]=0;
        }
        for(int t=1 ;t<x;t++){
            for(int s=0;s<numTags;s++){
                double max=-1;
                int k=0;
                double temp=0;
                double e = emitionProb(symbols[t],tags.get(s).label);
                for(int i=0;i<numTags;i++){
                    temp=viterbi[i][t-1]+transProb[i][s]+e;
                    temp=Math.exp(temp);
                    if(temp>max){
                        max=temp;
                        k=i;
                        
                    }
                }
                viterbi[s][t]=Math.log(max);
                backPointer[s][t]=k;
                //System.out.println(max);
            }
        }
        int pointer=0;
        double max=-1;
        for(int i=0;i<numTags;i++){
            if(Math.exp(viterbi[i][x-1])>max){
                pointer=i;
                max=Math.exp(viterbi[i][x-1]);
            }
        }
        //backtracking
        String[] res = new String[x];
        //res[x-1]=tags.get(pointer).label;
        for(int i=x-1;i>=0;i--){
            res[i]=tags.get(pointer).label;
            pointer=backPointer[pointer][i];
            
            
        }
        
        for(int i=0;i<res.length;i++){
            System.out.print(res[i]+"  ");
        }
        
        //backtrack(backPointer,x-1,pointer);
       // System.out.println("");
        for(int i=0;i<x;i++){
            if(res[i].equals(tagsSequence[i]))
                n++;
            else{
                String ite[] = res[i].split("-");
                for(String re:ite){
                    if(re.equals(tagsSequence[i])){
                        n++;break;}
                    
                }
            }
        }
        System.out.println(n);
        return n;
    }

    double emitionProb(String symbol, String s) {
        
            Iterator<Word> it = words.listIterator();
            int cc=0;
            Word w;
            while(it.hasNext()){
                w=it.next();
                if(symbol.equals(w.word)){
                    cc=1;
                    Iterator<Label> i = w.labels.listIterator();
                    while(i.hasNext())
                    {
                        Label l =i.next();
                        if(l.label.equals(s))
                            return Math.log(l.prob);
                    }
                }
            }
            if(cc==0)
                return 0;
            else
                return -100;
    }

    private void backtrack(int[][] backPointer, int i,int j) {
        if(i==0)
            return;
        backtrack(backPointer, i-1, backPointer[j][i-1]);
        System.out.print(tags.get(j).label+ " ");
                    
    }
}
