/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabinizer.automata;

/**
 *
 * @author jkretinsky
 */
public interface AccAutomatonInterface {

    String toHOA();

    String toDotty();

    String acc();

    int size();
    
    int pairNumber();

}
