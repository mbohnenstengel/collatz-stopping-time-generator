/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 * 
 * Models a shared data object. 
 *  
 * @author treichherzer
 */
public class SharedData {

    private int _g = 0;
	private int _maxValue;
	
	private static int MAX_VALUE = 10000;
	
	public SharedData(int maxValue)
	{
		_maxValue = maxValue;
	}
	
	public SharedData()
	{
		_maxValue = MAX_VALUE;
	}

    public void incrementValue() {
        _g++;
    }

    public int getValue() {
        return _g;
    }
	
	public int getRange() {
		return _maxValue;
	}
}
