package sample;

public class SizeToString {
    static String convert(float size){
        String result="";
        if(size<1024) result = (long)size + " bytes";
        else if((size/=1024) < 1024) result = String.format("%.2f Kbytes", size);
        else if((size/=1024) < 1024) result = String.format("%.2f Mbytes", size);
        else if((size/=1024) < 1024) result = String.format("%.2f Gbytes", size);
        else if((size/=1024) < 1024) result = String.format("%.2f Tbytes", size);
        else result = "Unknown size";
        return result;
    }
}
