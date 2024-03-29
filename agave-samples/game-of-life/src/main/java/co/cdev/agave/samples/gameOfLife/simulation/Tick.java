/**
 * Copyright (c) 2008, Damian Carrillo
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted 
 * provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice, this list of 
 *     conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of 
 *     conditions and the following disclaimer in the documentation and/or other materials 
 *     provided with the distribution.
 *   * Neither the name of the copyright holder's organization nor the names of its contributors 
 *     may be used to endorse or promote products derived from this software without specific 
 *     prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package co.cdev.agave.samples.gameOfLife.simulation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:damiancarrillo@gmail.com">Damian Carrillo</a>
 */
public class Tick {

    private int count;
    private boolean stable;
    private List<Position> aliveToDead = new ArrayList<Position>();
    private List<Position> deadToAlive = new ArrayList<Position>();
    
    public Tick() {}
    
    public Tick advance(List<List<Cell>> previous, List<List<Cell>> current) {
        
        aliveToDead.clear();
        deadToAlive.clear();
        boolean anyCellsAlive = false;
        
        for (int row = 0; row < current.size(); row++) {
            for (int column = 0; column < current.get(row).size(); column++) {
                if (previous.get(row).get(column).getState() == State.DEAD 
                    && current.get(row).get(column).getState() == State.ALIVE) {
                    deadToAlive.add(current.get(row).get(column).getPosition());
                } else if (previous.get(row).get(column).getState() == State.ALIVE 
                    && current.get(row).get(column).getState() == State.DEAD) {
                    aliveToDead.add(current.get(row).get(column).getPosition());
                }
                if (current.get(row).get(column).isAlive()) {
                    anyCellsAlive = true;
                }
            }
        }
        
        ++count;
        
        if (aliveToDead.isEmpty() && deadToAlive.isEmpty() || !anyCellsAlive) {
            stable = true;
        }
        return this;
    }

    public int getCount() {
        return count;
    }
    
    public boolean isStable() {
        return stable;
    }

    public List<Position> getAliveToDead() {
        return aliveToDead;
    }

    public List<Position> getDeadToAlive() {
        return deadToAlive;
    }

}
