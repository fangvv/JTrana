package src.gui;

import java.util.*;

import javax.swing.SwingUtilities;

/** Simple memory monitor.
 * <p>
 * $Id: MemoryMonitor.java
 * 
 * 
 */
public class MemoryMonitor extends javax.swing.JProgressBar {
    /**
	 * 
	 */
	private static final long serialVersionUID = 8946094047539782268L;

	int total;

    int used;

    int oldUsed;

    final int diff = 10;

    Runtime runtime = Runtime.getRuntime();

    Timer timer = new Timer(true);

    public MemoryMonitor() {
        super();
        setMinimum(0);
        setStringPainted(true);
        
        /* A reusable runnable */
        final Runnable update = new Runnable() {
            public void run() {
                setMaximum(total);
                setValue(used);
                setString(used + " Kb / " + total + " Kb");                
            }
        };

        timer.schedule(new TimerTask() {
            public void run() {
                total = (int) (runtime.totalMemory() / 1024);
                used = total - (int) (runtime.freeMemory() / 1024);
                if (used < oldUsed - diff || used > oldUsed + diff) {                    
                    SwingUtilities.invokeLater(update);
                    oldUsed = used;
                }
            }
        }, 9, 2000);
    }
}