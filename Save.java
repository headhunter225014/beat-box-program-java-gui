import java.io.*;

public class Save implements Serializable
{
    private int width;
    private int lengtth;

    public void setWidth(int w)
    {
        width = w;
    }

    public void setLengtth(int l)
    {
        lengtth = l;
    }

    public static void main(String[] args) {
        Save save = new Save();
        save.setWidth(50);
        save.setLengtth(90);

        try
        {
            FileOutputStream fo = new FileOutputStream("save.ser");
            ObjectOutputStream ob = new ObjectOutputStream(fo);
            ob.writeObject(save);
            ob.close();
        }
        catch (Exception e)
        {

        }
    }
}
