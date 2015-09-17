package io.github.hsyyid.payday.utils;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import io.github.hsyyid.payday.Main;
import ninja.leaping.configurate.ConfigurationNode;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class Utils 
{
    public static TimeUnit getTimeUnit()
    {
        ConfigurationNode valueNode = Main.config.getNode((Object[]) ("timeunit").split("\\."));
        
        if(valueNode.getValue() != null)
        {
            String value = valueNode.getString();
            
            if(value.toLowerCase().equals("days"))
            {
                return TimeUnit.DAYS;
            }
            else if(value.toLowerCase().equals("hours"))
            {
                return TimeUnit.HOURS;
            }
            else if(value.toLowerCase().equals("minutes"))
            {
                return TimeUnit.MINUTES;
            }
            else if(value.toLowerCase().equals("seconds"))
            {
                return TimeUnit.SECONDS;
            }
            else if(value.toLowerCase().equals("microseconds"))
            {
                return TimeUnit.MICROSECONDS;
            }
            else if(value.toLowerCase().equals("milliseconds"))
            {
                return TimeUnit.MILLISECONDS;
            }
            else if(value.toLowerCase().equals("nanoseconds"))
            {
                return TimeUnit.NANOSECONDS;
            }
            else
            {
                System.out.println("Error! TimeUnit not recognized: " + value);
                return TimeUnit.HOURS;
            }
        }
        else
        {
            Utils.setTimeUnit("Hours");
            return TimeUnit.HOURS;
        }
    }
    
    public static void setTimeUnit(String timeunit)
    {
        ConfigurationLoader<CommentedConfigurationNode> configManager = Main.getConfigManager();
        Main.config.getNode("timeunit").setValue(timeunit);
        
        try
        {
            configManager.save(Main.config);
            configManager.load();
        }
        catch (IOException e)
        {
            System.out.println("Failed to save TimeUnit!");
        }
    }
}
