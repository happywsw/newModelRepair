package alphaDollar;

import java.util.ArrayList;

import org.processmining.framework.models.petrinet.Transition;

public class CoExistAutomaton {
	private int status;
	
	ArrayList<String> ASet;
	ArrayList<String> BSet;
	ArrayList<String> joinSet;
	Transition t1;
	Transition t2;
	
	public static final int ANYISOK = 0;
	public static final int NeedA = 1;
	public static final int NeedB = 2;
	public static final int AFTERMEETJOIN_ANYISOK = 3;
	
	public boolean isCoexist;
	
	public CoExistAutomaton(ArrayList<String> _ASet, ArrayList<String> _BSet, Transition _t1, Transition _t2)
	{
		isCoexist = true;
		status = ANYISOK;
		ASet = (ArrayList<String>) _ASet.clone();
		BSet = (ArrayList<String>) _BSet.clone();
		
		joinSet = AlphaMMinerDataUtil.join(ASet, BSet);
		ASet =AlphaMMinerDataUtil.except(ASet,joinSet);
		BSet =AlphaMMinerDataUtil.except(BSet,joinSet);
		t1 = _t1;
		t2 = _t2;
	}
		
	public boolean isCombinable()
	{
		return isCoexist;
	}
	
	public void add(String task)
	{
		if (task.equals("newtrace"))	//新的trace,在上�?个trace的时候完成了共现才可�?
		{
			switch (status)
			{
				case ANYISOK:					
					break;
				case NeedA:
					isCoexist = false;
					break;
				case NeedB:
					isCoexist = false;
					break;
				case AFTERMEETJOIN_ANYISOK:
					isCoexist = false;
					break;
			}
		}
		else if (ASet.contains(task))	//A的集�?
		{
			switch (status)
			{
				case ANYISOK:
					status = NeedB;
					break;
				case NeedA:
					status = ANYISOK;
					break;
				case NeedB:
					isCoexist = false;
					break;
				case AFTERMEETJOIN_ANYISOK:
					status = ANYISOK;
					break;
			}
		}
		else if (BSet.contains(task))	//B的集�?
		{
			switch (status)
			{
				case ANYISOK:
					status = NeedA;
					break;
				case NeedA:
					isCoexist = false;
					break;
				case NeedB:
					status = ANYISOK;
					break;
				case AFTERMEETJOIN_ANYISOK:
					status = ANYISOK;
					break;
			}
		}
		else if (joinSet.contains(task))	//两�?�的交际
		{
			switch (status)
			{
				case ANYISOK:
					status = AFTERMEETJOIN_ANYISOK;
					break;
				case NeedA:
					status = ANYISOK;
					break;
				case NeedB:
					status = ANYISOK;
					break;
				case AFTERMEETJOIN_ANYISOK:
					status = ANYISOK;
					break;
			}
		}									
	}
		
}
