package org.tal.sensorlibrary;

import java.util.Calendar;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class daytime extends Circuit {
    enum TimeField {
        SECOND(23999,59), SECONDOFDAY(23999, 86399), MINUTE(59,59), MINUTEOFDAY(1439,1439), HOUR(23, 23), TICK(23999, 86399);

        public int gameMax, earthMax;

        TimeField(int gameMax, int earthMax) {
            this.gameMax = gameMax;
            this.earthMax = earthMax;
        }

        int maxTime(boolean earthtime) {
            if (earthtime) return earthMax;
            else return gameMax;
        }
    }

    private static final double ticksPerHour = 1000d;
    private static final double ticksPerMinute = ticksPerHour/60d; //16.6666666...

    private boolean earthtime = false;
    private int maxval;

    private TimeField timeField;
    private World w;

    @Override
    public void inputChange(int inIdx, boolean state) {
        if (state) {
            int time;
            if (earthtime) {
                Calendar now = Calendar.getInstance();
                if (timeField==TimeField.SECOND)
                    time = now.get(Calendar.SECOND);
                else if(timeField == TimeField.SECONDOFDAY || timeField==TimeField.TICK)
                    time = now.get(Calendar.SECOND) + now.get(Calendar.MINUTE)*60 + now.get(Calendar.HOUR_OF_DAY) * 60;
                else if (timeField==TimeField.MINUTE)
                    time = now.get(Calendar.MINUTE);
                else if (timeField==TimeField.MINUTEOFDAY)
                    time = now.get(Calendar.MINUTE) + now.get(Calendar.HOUR_OF_DAY)*60;
                else if (timeField==TimeField.HOUR)
                    time = now.get(Calendar.HOUR_OF_DAY);
				// Hinchy edits here!
                else if (timeField==TimeField.HOUR1)
                    value = now.get(Calendar.HOUR_OF_DAY);
					time = value % 10;
                else if (timeField==TimeField.HOUR10)
                    value = now.get(Calendar.HOUR_OF_DAY);
					time = value - (value % 10);
                else if (timeField==TimeField.MINUTE1)
                    value = now.get(Calendar.MINUTE);
					time = value % 10;
                else if (timeField==TimeField.MINUTE10)
                    value = now.get(Calendar.MINUTE);
					time = value - (value % 10);
                else time = -1;
            } else {
                if (timeField==TimeField.SECONDOFDAY || timeField==TimeField.TICK || timeField==TimeField.SECOND)
                    time = (int)w.getTime();
                else if (timeField == TimeField.MINUTEOFDAY)
                    time = (int)Math.round(w.getTime()/ticksPerMinute);
                else if (timeField == TimeField.MINUTE)
                    time = (int)Math.round((w.getTime()%1000)/ticksPerMinute);
                else if (timeField == TimeField.HOUR)
                    time = (int)(w.getTime()/ticksPerHour);
				// Hinchy edits here!
                else if (timeField==TimeField.HOUR1)
                    value = (int)(w.getTime()/ticksPerHour);
					time = value % 10;
                else if (timeField==TimeField.HOUR10)
                    value = (int)(w.getTime()/ticksPerHour);
					time = value - (value % 10);
                else if (timeField==TimeField.MINUTE1)
                    value = (int)Math.round((w.getTime()%1000)/ticksPerMinute);
					time = value % 10;
                else if (timeField==TimeField.MINUTE10)
                    value = (int)Math.round((w.getTime()%1000)/ticksPerMinute);
					time = value - (value % 10);
                else time = -1;
            }

            time = Math.min(time, timeField.maxTime(earthtime));
            
            if (hasDebuggers()) debug("Time is " + time);

            int output;
            if (time>maxval)
                output = Math.round(((float)time/(float)timeField.maxTime(earthtime))*maxval);
            else
                output = time;

            sendInt(0, outputs.length, output);
        }
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        if (inputs.length!=1) {
            error(sender, "Expecting 1 clock input.");
            return false;
        }

        if (args.length>0) {
            if (args[0].equalsIgnoreCase("earthtime")) earthtime = true;
            else if (args[0].equalsIgnoreCase("gametime")) earthtime = false;
        }

        if (args.length>1) {
            try {
                timeField = TimeField.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException ie) {
                error(sender, "Unknown time field: " + args[1]);
                return false;
            }
        } else {
            timeField=TimeField.TICK;
        }
        
        if (args.length>2) {
            w = redstoneChips.getServer().getWorld(args[2]);
            if (w == null) {
                error(sender, "Unknown world name: " + args[2]);
                return false;
            }
        } else {
            w = world;
        }

        maxval = (int)(Math.pow(2, outputs.length)-1);        

        return true;
    }

}
