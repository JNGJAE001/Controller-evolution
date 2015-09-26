package za.redbridge.controller.SANE;

import java.io.Serializable;

//connection definition containing label of the connected node and the weight
public class Connection implements Serializable
{
    //describes which node to connect to
    int label;

    //describes the weight value of the connection
    double weight;

    public Connection(int l, double w)
    {
        label = l;
        weight = w;
    }

    //copy constructor
    public Connection(Connection c)
    {
        this.label = c.label;
        this.weight = c.weight;
    }

    //copies values from other connection
    public void set(Connection c)
    {
        this.label = c.label;
        this.weight = c.weight;
    }

    public void set(int l, double w)
    {
        label = l;
        weight = w;
    }

    public int getLabel()
    {
        return label;
    }
    public double getWeight()
    {
        return weight;
    }

    @Override
    public boolean equals(Object other)
    {
        return this.label == ((Connection) other).getLabel();
    }

    @Override
    public int hashCode()
    {
        return label;
    }
}
