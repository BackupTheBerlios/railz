/*
 * Copyright (C) 2006 Robert Tuck
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */


package org.railz.world.top;

import java.util.Random;

/**
 * Represents a Universally Unique Identifier which uniquely identifies an 
 * object in the game-world. This is loosely based on the time based UUID defined
 * in RFC4122 (with slight variations). Implemented here for the benefit of Java 1.4 
 * which doesn't have the UUID class in the SDK
 * @author bob
 */
public class UUID {
    private long idPart1;
    private long idPart2;
    
    private long lastCalled;
    private static int callsThisMillisecond;
    private static final Object mutex = new Integer(1);
    private static int clock_sequence;
    
    /* generated randomly because we don't have access to the MAC address */
    private static final int node_2_5;
    private static final int node_0_1;
    
    static {
        // use normal not secure random since not intended to be used for 
        // authentication/encryption
        Random rand = new Random(System.currentTimeMillis());
        clock_sequence = rand.nextInt();
        node_2_5 = rand.nextInt();
        node_0_1 = rand.nextInt() & 0xFFFF;
    }
    
    /** Creates a new instance of UUID */
    public UUID() {
        // Note that time is relative to UNIX epoch (1970) rather than 1582 AD
        // as per RFC 4122
        long now = System.currentTimeMillis();
        synchronized (mutex) {
            if (now == lastCalled) {
                callsThisMillisecond++;
            } else {
                callsThisMillisecond = 0;
                lastCalled = now;
            }
            clock_sequence++;
        }
        
        // millis to nanos       
        now *= 1000000;                
        int time_low = ((int) (now & 0xFFFFFFFF)) | callsThisMillisecond;
        int time_mid = ((int) (now >> 16)) & 0xFFFF0000;
        int time_high_version = 0x1000 | (int) (now >> 48);
        int time_mid_high_version = time_mid | time_high_version;
        idPart1 = (time_low << 32) | time_mid_high_version;
        
        int clock_seq_hi_res_lo_res_node_0_2 = 
                ((clock_sequence & 0xFF) << 24) |
                ((clock_sequence & 0xFF00) << 8) |
                node_0_1;
        
        idPart2 = (clock_seq_hi_res_lo_res_node_0_2 << 32) |
                node_2_5;
    }
        
    public boolean equals(Object o) {
        if (o instanceof UUID) {
            UUID u = (UUID) o;
            return idPart1 == u.idPart1 && idPart2 == u.idPart2;         
        }
        return false;
    }
    
    public int hashCode() {
        return ((int) (idPart1 >> 32)) ^ ((int) (idPart1 & 0xFFFFFFFF)) 
            ^ ((int) (idPart2 >> 32)) ^ ((int) (idPart2 & 0xFFFFFFFF));
    }
}
