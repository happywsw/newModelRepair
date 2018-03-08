package com.modelrepair.relation;

import com.iise.shudi.bp.BehavioralProfileSimilarity;
import com.modelrepair.util.FileUtil;

import org.jbpt.bp.BehaviouralProfile;
import org.jbpt.bp.RelSetType;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.Transition;
import org.jbpt.petri.Place;
import org.jbpt.petri.io.PNMLSerializer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.io.File;

/**
 * 
 */
public class BPPlusSimilarity {
	//store all silent transitions's Name
	ArrayList<String> silentNames = new ArrayList<String>();
	//sotre all silent transitions
	ArrayList<Transition> silentTransitons = new ArrayList<Transition>();
    //store all last transitions of each loop
    private HashMap<NetSystem, ArrayList<Transition>> htLastNodes = new HashMap();
    //store all relation matrices of each net
    protected HashMap<NetSystem, HashMap<Transition, HashMap<Transition, Integer>>> htMatrices = new HashMap();
    //store all relation matrices of each net by transitions
    private HashMap<NetSystem, HashMap<String, HashMap<String, Integer>>> htMatricesTrans = new HashMap();
    //store all presets of each place
    private HashMap<NetSystem, HashMap<Place, Transition[]>> htPreOfPlace = new HashMap();
    //store all postsets of each place
    private HashMap<NetSystem, HashMap<Place, Transition[]>> htPostOfPlace = new HashMap();
    //store all presets of each transition
    private HashMap<NetSystem, HashMap<Transition, Place[]>> htPreOfTrans = new HashMap();
    //store all postsets of each transition
    private HashMap<NetSystem, HashMap<Transition, Place[]>> htPostOfTrans = new HashMap();
    //store all places of a net
    private HashMap<NetSystem, Place[]> htPlaces = new HashMap();
    //store all transitions of a net
    private HashMap<NetSystem, Transition[]> htTransitions = new HashMap();
    //store all preset of each node
    private HashMap<NetSystem, HashMap<Node, Node[]>> htPresets = new HashMap();
    //store all postsets of each node
    private HashMap<NetSystem, HashMap<Node, Node[]>> htPostsets = new HashMap();
    //store all -->,->> of one transition
    private HashMap<NetSystem, HashMap<Transition, ArrayList<Transition>>> htCausals = new HashMap();
    //store all ||,||- of one transition
    private HashMap<NetSystem, HashMap<Transition, ArrayList<Transition>>> htConcurrs = new HashMap();
    //store all # of one transition
    private HashMap<NetSystem, HashMap<Transition, ArrayList<Transition>>> htConflicts = new HashMap();

    public BPPlusSimilarity(){}
    
    public BPPlusSimilarity(ArrayList<String> silentNames){
    	this.silentNames = silentNames;
    }
    /**
     * initialize all variables relevant to a given net
     * e.g., preset, postset of any node, relation matrix
     * @param net, a given Petri net
     */
    public void initialize(NetSystem net)
    {
        //restore all global variables
        htLastNodes.remove(net);
        htMatrices.remove(net);
        htMatricesTrans.remove(net);
        htPreOfPlace.remove(net);
        htPostOfPlace.remove(net);
        htPreOfTrans.remove(net);
        htPostOfTrans.remove(net);
        htPlaces.remove(net);
        htTransitions.remove(net);
        htPresets.remove(net);
        htPostsets.remove(net);
        htCausals.remove(net);
        htConcurrs.remove(net);
        htConflicts.remove(net);

        //initialize the array for last nodes
        ArrayList<Transition> alLastNodes = new ArrayList();
        htLastNodes.put(net, alLastNodes);

        //get all transitions
        Set<Transition> transList = net.getTransitions();
        Transition[] arTrans = new Transition[transList.size()];
        transList.toArray(arTrans);
        //store all transitions
        htTransitions.put(net, arTrans);

        //get all places
        Set<Place> placeList = net.getPlaces();
        Place[] arPlaces = new Place[placeList.size()];
        placeList.toArray(arPlaces);
        //store all places
        htPlaces.put(net, arPlaces);

        //Initialize relation matrix, causal/concurr/conflict matrix
        HashMap<Transition, HashMap<Transition, Integer>> htRM = new HashMap();
        HashMap<Transition, ArrayList<Transition>> htCausalM = new HashMap();
        HashMap<Transition, ArrayList<Transition>> htConcurrM = new HashMap();
        HashMap<Transition, ArrayList<Transition>> htConflictM = new HashMap();
        
        //store net's RM, CausalM, ConcurrM, ConflictM
        htMatrices.put(net, htRM);
        htCausals.put(net, htCausalM);
        htConcurrs.put(net, htConcurrM);
        htConflicts.put(net, htConflictM);
        
        for(Transition trans:arTrans)
        {
            HashMap<Transition, Integer> htRelations = new HashMap();
            //store one transition's all relations
            htRM.put(trans, htRelations);

            ArrayList<Transition> alCausal = new ArrayList();
            //store one transiton's all causal relations
            htCausalM.put(trans, alCausal);
            ArrayList<Transition> alConcurr = new ArrayList();
            //store one transiton's all concurrency relations
            htConcurrM.put(trans, alConcurr);
            ArrayList<Transition> alConflict = new ArrayList();
            //store one transiton's all conflict relations
            htConflictM.put(trans, alConflict);
        }

        //store all places' presets and postsets
        HashMap<Node, Node[]> htPreNode = new HashMap();
        htPresets.put(net, htPreNode);
        HashMap<Place, Transition[]> htPrePlace = new HashMap();
        htPreOfPlace.put(net, htPrePlace);
        HashMap<Node, Node[]> htPostNode = new HashMap();
        htPostsets.put(net, htPostNode);
        HashMap<Place, Transition[]> htPostPlace = new HashMap();
        htPostOfPlace.put(net, htPostPlace);
        
        for(Place place : arPlaces)
        {
            //restore its numbers
            place.setTag(null);
            //get input transitions
            Set<Transition> setInputs = net.getPreset(place);
            Transition[] arInput = new Transition[setInputs.size()];
            setInputs.toArray(arInput);
            //store its preset
            htPreNode.put(place, arInput);    //node include place + transition
            htPrePlace.put(place, arInput);

            //get output transitions
            Set<Transition> setOutputs = net.getPostset(place); //place connection transition
            Transition[] arOutput = new Transition[setOutputs.size()];
            setOutputs.toArray(arOutput);
            //store its postset
            htPostNode.put(place, arOutput);
            htPostPlace.put(place, arOutput);
        }

        //store all transitions' presets and postsets
        HashMap<Transition, Place[]> htPreTrans = new HashMap();
        htPreOfTrans.put(net, htPreTrans);
        HashMap<Transition, Place[]> htPostTrans = new HashMap();
        htPostOfTrans.put(net, htPostTrans);
        
        for(Transition trans: arTrans)
        {
            //restore its numbers
            trans.setTag(null);
            //get input places
            Set<Place> setInputs = net.getPreset(trans);
            Place[] arInput = new Place[setInputs.size()];
            setInputs.toArray(arInput);
            //store its preset
            htPreNode.put(trans, arInput);
            htPreTrans.put(trans, arInput);

            //get output places
            Set<Place> setOutputs = net.getPostset(trans);
            Place[] arOutput = new Place[setOutputs.size()];
            setOutputs.toArray(arOutput);
            //store its postset
            htPostNode.put(trans, arOutput);
            htPostTrans.put(trans, arOutput);
        }
    }

    /**
     * try to find all last transitions of loops
     * using a deep first search of a directed graph
     * @param net
     */
    private void findLoops(NetSystem net)
    {
        ArrayList<Place> alPlaces = new ArrayList();
        System.out.println(net.getSourcePlaces().size());
        alPlaces.addAll(net.getSourcePlaces());
        //try to find one loop
        Node start = alPlaces.get(0);
        Set<Node> global = new HashSet();
        findALoop(net, start, global);
    }

    /**
     * try to find a loop recursively
     * @param net, the traversal net
     * @param start, the current traversal node
     */
    private void findALoop(NetSystem net, Node start, Set<Node> global)
    {
        //if the node has been visited globally
        if (global.contains(start))
            return;
        //store it in the global set
        global.add(start);

        start.setTag(new Integer(1));
        Node[] arSucc = htPostsets.get(net).get(start);
        for (Node succ : arSucc)
        {
            if(succ.getTag() != null)
            {
                //find an entry place of a loop
                if(start instanceof Transition && !htLastNodes.get(net).contains(start))
                    htLastNodes.get(net).add((Transition)start);
            }
            else
            {
                //try to find a loop recursively
                findALoop(net, succ, global);
            }
        }
        start.setTag(null);
    }

    /**
     * set numbers of nodes in a breadth-first traversal order
     * consider xor-join (including loop) and and-join
     * @param net, a given Petri net, numbers will be stored in tags
     * @param isNumbering, whether the numbering procedure is called
     */
    public void numberingNodes(NetSystem net, boolean isNumbering)
    {
        //find all last nodes of loops first
        findLoops(net);
       // System.out.println("flag:" + isNumbering);
        //stop numbering if it not needed
        if(!isNumbering){
        //	System.out.println("return.......");
            return;
        }

        ArrayList<Node> queue = new ArrayList();
        queue.addAll(net.getSourceNodes());
        while(queue.size()>0)
        {
            Node curr = queue.get(0);    //the curr node
            Node[] preset = htPresets.get(net).get(curr);
            //try to give this node a number
            if(preset.length == 0)  //source place
            {
                curr.setTag(new Integer(0));
                addSucc(net, queue, curr);
            }
            else if(preset.length == 1) //only one predecessor
            {
                Node pred = preset[0];
                Integer num = (Integer)pred.getTag();
                curr.setTag(new Integer(num.intValue()+1));
                addSucc(net, queue, curr);
            }
            else //has more than one predecessors
            {
                boolean allPredDone = true;
                int iNum = 0;
                for(Node pred: preset)
                {
                    Integer num = (Integer)pred.getTag();
                    //not ready for numbering this node
                    if(num == null)
                    {
                        if(!htLastNodes.get(net).contains(pred))
                            allPredDone = false;
                    }
                    else
                    {
                        //find the max number of its predecessors
                        if(iNum < num.intValue())
                            iNum = num.intValue();
                    }
                }

                //all predecessors' numbers are ready
                if(allPredDone)
                {
                    curr.setTag(new Integer(iNum+1));
                    addSucc(net, queue, curr);
                }
                else
                {
                    //push to the last in queue
                    queue.remove(curr);
                    queue.add(curr);
                }
            }
        }
    }

    protected static HashMap<Transition, HashMap<Transition, Integer>> cloneRM(HashMap<Transition, HashMap<Transition, Integer>> htRM)
    {
        //clone the hashmap itself
        HashMap<Transition, HashMap<Transition, Integer>> htRMClone = new HashMap();
        //clone each hashmap in the value set
        Set<Transition> alKeys = htRM.keySet();
        for(Transition trans: alKeys)
            htRMClone.put(trans, (HashMap)htRM.get(trans).clone());

        return htRMClone;
    }

    protected static HashMap<Transition, ArrayList<Transition>> cloneCausal(HashMap<Transition, ArrayList<Transition>> htCausalM)
    {
        //clone the hashmap itself
        HashMap<Transition, ArrayList<Transition>> htCausalMClone = new HashMap();
        //clone each hashmap in the value set
        Set<Transition> alKeys = htCausalM.keySet();
        for(Transition trans: alKeys)
            htCausalMClone.put(trans, (ArrayList)htCausalM.get(trans).clone());

        return htCausalMClone;
    }

    /**
     * deduce all task relations and store them in htMatrices
     * @param net, a given Petri net with numbers stored in tags
     */
    public void deduceRelationMatrix(NetSystem net)
    {
        HashMap<Transition, HashMap<Transition, Integer>> htRM = htMatrices.get(net);
        HashMap<Transition, ArrayList<Transition>> htCausalM = htCausals.get(net);
        HashMap<Transition, ArrayList<Transition>> htConcurrM = htConcurrs.get(net);
        HashMap<Transition, ArrayList<Transition>> htConflictM = htConflicts.get(net);
        HashMap<Transition, ArrayList<Transition>> htDirect = new HashMap<Transition, ArrayList<Transition>>();
        HashMap<Transition, ArrayList<Transition>> htIndirect = new HashMap<Transition, ArrayList<Transition>>();
        //1: deduce -->,# from places and || from transitions

        //1.1: deduce -->,# from places
        Place[] arPlaces = htPlaces.get(net);
        for(Place p: arPlaces)
        {
        	System.out.println(p.getLabel());
            Transition[] alInput = htPreOfPlace.get(net).get(p);
            Transition[] alOutput = htPostOfPlace.get(net).get(p);

            //1.1.1 deduce # between input transitions
            for(int i=0; i<alInput.length; i++)
            {
                //must not loop back
               if(htLastNodes.get(net).contains(alInput[i]))
                   continue;
                HashMap<Transition, Integer> htRelI = htRM.get(alInput[i]);
                for (int j = i+1; j < alInput.length; j++)
                {
                    //must not loop back
                   if(htLastNodes.get(net).contains(alInput[j]))
                        continue;
                    HashMap<Transition, Integer> htRelJ = htRM.get(alInput[j]);
                    if(htRelI.get(alInput[j]) == null)
                    {
                        htRelI.put(alInput[j], Relation.CONFLICT);
                        htRelJ.put(alInput[i], Relation.CONFLICT);
                        //store the # relation between arInput[i] and arInput[j]
                        htConflictM.get(alInput[i]).add(alInput[j]);
                        htConflictM.get(alInput[j]).add(alInput[i]);
                    }
                }
            }

            //1.1.2 deduce # between output transitions
            for(int i=0; i<alOutput.length; i++)
            {
                HashMap<Transition, Integer> htRelI = htRM.get(alOutput[i]);
                for (int j = i+1; j < alOutput.length; j++)
                {
                    HashMap<Transition, Integer> htRelJ = htRM.get(alOutput[j]);
                    if(htRelI.get(alOutput[j]) == null)
                    {
                        htRelI.put(alOutput[j], Relation.CONFLICT);
                        htRelJ.put(alOutput[i], Relation.CONFLICT);
                        //store the # relation between alOutput[i] and alOutput[j]
                        htConflictM.get(alOutput[i]).add(alOutput[j]);
                        htConflictM.get(alOutput[j]).add(alOutput[i]);
                    }
                }
            }

            //1.1.3 deduce --> between input/output transitions
            for(int i=0; i<alInput.length; i++)
            {
                //must not loop back
              //  if (htLastNodes.get(net).contains(alInput[i]))
               //     continue;
                HashMap<Transition, Integer> htRelI = htRM.get(alInput[i]);
                for (int j = 0; j < alOutput.length; j++)
                {
                    htRelI.put(alOutput[j], Relation.DIRECTCAUSAL);
                    //store the --> relation here
                    if(!htDirect.containsKey(alInput[i]))
                    	htDirect.put(alInput[i], new ArrayList<>());
                    System.out.println(alInput[i]+"->"+alOutput[j]);
                    htDirect.get(alInput[i]).add(alOutput[j]);
                    
                    htCausalM.get(alInput[i]).add(alOutput[j]);
                }
            }
        }

        //2: deduce ->> according to transitive -->
        //move up beore 1.2 if net is non-free-choice
        Transition[] arTrans = htTransitions.get(net);
        deduceIndirectCausals(htRM, htCausalM, arTrans, htIndirect);
        //3: deduce # according to # and (--> or ->>)
        //move up beore 1.2 if net is non-free-choice
        deduceCons(htRM, htCausalM, htConflictM, arTrans, Relation.CONFLICT);

        //1.2: deduce || from all transitions
        for(Transition trans: arTrans)
        {
            //check for all the input places
            deduceConcurrs(net, htRM, htPreOfTrans, htPreOfPlace, htConcurrM, trans);

            //check for all the output places
            deduceConcurrs(net, htRM, htPostOfTrans, htPostOfPlace, htConcurrM, trans);
        }

        //4: deduce || according to || and (--> or ->>)
        deduceCons(htRM, htCausalM, htConcurrM, arTrans, Relation.CONCURRENCY);

        //New:: fix --> involved non-free-choice to |-> if necessary
        fixWrongDirectCausal(net, htRM, htConcurrM, htCausalM, arPlaces);
        
        
        //5: deduce -->,->>,# according to loops
        ArrayList<Transition> alLastNodes = htLastNodes.get(net);

        //5.1: add --> for last transition one by one
        ArrayList<HashMap<Transition, HashMap<Transition, Integer>>> alNewRM = new ArrayList();
        for(Transition lastNode: alLastNodes)
        {
            //clone the two relation sets for each last transition
            HashMap<Transition, HashMap<Transition, Integer>> htRMClone = cloneRM(htRM);
            HashMap<Transition, ArrayList<Transition>> htCausalMClone = cloneCausal(htCausalM);
            //store the cloned new relation sets
            alNewRM.add(htRMClone);

            //get lastNode's output places
            Place[] arOutput = htPostOfTrans.get(net).get(lastNode);
            //enumerate each output place for successors
            for(Place place: arOutput)
            {
                //get place's output transitions
                Transition[] arSucc = htPostOfPlace.get(net).get(place);
                //enumerate each successor of lastNode
                for(Transition succ: arSucc)
                {
                    if(htRMClone.get(lastNode).get(succ) != null)
                        continue;
                    //add --> between lastNode and succ
                    htRMClone.get(lastNode).put(succ, Relation.DIRECTCAUSAL);
                    //store --> between lastNode and succ
                    htCausalMClone.get(lastNode).add(succ);
                }
            }

            //deduce ->> for all transitions caused by last transition
            deduceIndirectCausals(htRMClone, htCausalMClone, arTrans, htIndirect);
        }
        
      
        
        //5.2: merge all new causal relations together
        for(int i=alLastNodes.size()-1; i>=0; i--)
        {
            HashMap<Transition, HashMap<Transition, Integer>> htRMClone = alNewRM.get(i);

            for(Transition trans: arTrans)
            {
                //merge the new relations in htRMClone and htCausalMClone
                HashMap<Transition, Integer> htRelTransClone = htRMClone.get(trans);
                HashMap<Transition, Integer> htRelTrans = htRM.get(trans);
                //if they are not in the same size
                if(htRelTransClone.size() > htRelTrans.size())
                {
                    Set<Transition> alKeys = htRelTransClone.keySet();
                    for(Transition key: alKeys)
                    {
                        //merge a new relation in the clone
                        if(htRelTrans.get(key) == null)
                        {
                            htRelTrans.put(key, htRelTransClone.get(key));
                            //update the causal relations accordingly
                            htCausalM.get(trans).add(key);
                        }
                    }
                }
            }
        }
       
//        //5.3: add # for transitions not in a loop
//        for(Transition trans: arTrans)
//            if(htRM.get(trans).get(trans) == null)
//                htRM.get(trans).put(trans, Relation.CONFLICT);
//        
//     

       //6: deduce <--,<<-,<-| according to --> , ->> and |-> 
        for(Transition trans: arTrans)
        {
            ArrayList<Transition> arSucc = htCausalM.get(trans);
            //trans --> or ->> or |-> with succ
            for(Transition succ: arSucc)
            {
                //succ cannot reach to trans
                if(htRM.get(succ).get(trans) == null)
                {
                    Integer tr = htRM.get(trans).get(succ);
                    if(tr == Relation.DIRECTCAUSAL)
                        htRM.get(succ).put(trans, Relation.INVERSEDIRECTCAUSAL);
                    else if(tr== Relation.REACHABLE)
                        htRM.get(succ).put(trans, Relation.INVERSEREACHABLE);
                    else {
						htRM.get(succ).put(trans, Relation.INVERSEIMPLICTCAUSUAL);
					}
                }
            }
        }

        //7: deduce || according to #
        for(Transition transI: arTrans)
        {
            //transI should be a visible transition
            if(isSilentTrans(transI))
            {
                silentTransitons.add(transI);
                continue;
            }
            //check each || between transI and transJ
            ArrayList<Transition> alConcurr = htConcurrM.get(transI);
            for(Transition transJ: alConcurr)
            {
                //transI should also be a visible transition
                if(isSilentTrans(transJ))
                    continue;

                //find a silent transition st where and st#transJ and st||transI
                ArrayList<Transition> alConflict = htConflictM.get(transJ);
                for(Transition st: alConflict)
                {
                    //st should be a silent transition
                    if (!isSilentTrans(st))
                        continue;

                    //st must be not exclusive with transI
                    if(htRM.get(transI).get(st) != Relation.CONFLICT)
                    {
                        htRM.get(transI).put(transJ, Relation.CONCURRENCY);

                        break;
                    }
                }
            }
        }

        //8: deduce --> from ->> and <-- from <<- by silent transitions

        //8.1: generate all longest paths with only silent transitions
        ArrayList<ArrayList<Transition>> alSPTotal = new ArrayList();
        //setup a initial tag for visiting purpose
        for(Transition silentTrans: silentTransitons)
            silentTrans.setTag(new Integer(0));
        for(Transition silentTrans: silentTransitons)
        {
            //check if the silent transition has been visited
            if(silentTrans.getTag() == null)
                continue;

            //initialize silent paths for this silent transition
            ArrayList<ArrayList<Transition>> alSilentPaths = new ArrayList();
            //in default, there is only one silent path
            alSilentPaths.add(new ArrayList<Transition>());
            
            //try to find a silent transition backward
            backwardSilent(net, silentTrans, alSilentPaths);
            System.out.println("silent path:"+alSilentPaths.size());
            //remove the last silent transition in each path
            for(ArrayList<Transition> alSilentPath: alSilentPaths)
                alSilentPath.remove(alSilentPath.size()-1);

            //try to find a silent transition forward
            forwardSilent(net, silentTrans, alSilentPaths);

            //store silent paths for this silent transition
            alSPTotal.addAll(alSilentPaths);
        }

                
        

      //8.4: enumerating all silent paths with variant length
        HashMap<String, String> htVisitedPredSucc = new HashMap();
        for(ArrayList<Transition> alSilentPath: alSPTotal)
        {
            //the length of a silent path
            int nTotal = alSilentPath.size();
            //enumerating all subset of the silent path
            for(int len=1; len<=nTotal; len++)
            {
                int nCount = nTotal - len + 1;
                //enumerating all silent paths with length len
                for(int pos=0; pos<nCount; pos++)
                {
                    //visit the silent path from pos to pos+len-1
                    Transition pred = alSilentPath.get(pos);
                    Transition succ = alSilentPath.get(pos+len-1);

                    //check whether pred's pred ->> succ's succ
                    ArrayList<Transition> alPred = getSilentPred(net, pred);
                   
                    ArrayList<Transition> alSucc = getSilentSucc(net, succ);
                    ArrayList<Transition> silentTasks = getSilents(net,pred,succ);
                    
                    for(Transition transPred: alPred)
                    {
                        for(Transition transSucc: alSucc)
                        {
                            //check whether transPred ->> transSucc
                        	System.out.println(transPred.getLabel()+":"+transSucc.getLabel()+":"+htIndirect.get(transPred).contains(transSucc)+":"+htRM.get(transSucc).get(transPred));
                            //if(htRM.get(transPred).get(transSucc) != Relation.REACHABLE)
                             //   continue;
                            if(!htIndirect.get(transPred).contains(transSucc))
                            	continue;
                            
                            String strPredSucc = transPred.getId() + ":" + transSucc.getId();
                            if(htVisitedPredSucc.get(strPredSucc) == null)
                            {
                                //store the visited pair of transPred and transSucc
                                htVisitedPredSucc.put(strPredSucc, "");
                                //check whether the silent path is OK
                                boolean isDirect = backwardSilent(net, htRM, transPred, transSucc);
                                if(isDirect)
                                {
                                    //revise the relation from ->> to -->
                                    htRM.get(transPred).put(transSucc, Relation.DIRECTCAUSAL);
                                    //if possible, revise the inverse relation from <<- to <--
                                    if(htRM.get(transSucc).get(transPred) == Relation.INVERSEREACHABLE)
                                        htRM.get(transSucc).put(transPred, Relation.INVERSEDIRECTCAUSAL);
                                }
                            }
                            
                            
                        }
                    }
                }
            }
        }

    
      
        //a->a or a#a
        for(Transition trans: arTrans){
        	if(htRM.get(trans).get(trans) != null && htRM.get(trans).get(trans) >= Relation.L2LOOP && htRM.get(trans).get(trans) <= Relation.DIRECTCAUSAL )
        		htRM.get(trans).put(trans, Relation.DIRECTCAUSAL);
        	else
        		htRM.get(trans).put(trans, Relation.CONFLICT);
        }
         
      for(Transition trans: arTrans){
    	for(Transition trans2:arTrans){
    		if(!trans.equals(trans2)&& htDirect.containsKey(trans)&& htDirect.get(trans).contains(trans2)
    				&& htRM.get(trans).get(trans2) == Relation.CONFLICT){
    			htRM.get(trans).put(trans2, Relation.DIRECTCAUSAL);
    		}
    		if(!trans.equals(trans2) && htRM.get(trans).get(trans2) == Relation.REACHABLE){
    			htRM.get(trans).put(trans2, Relation.CONFLICT);
    			htRM.get(trans2).put(trans, Relation.CONFLICT);
    		}
    		else if(!trans.equals(trans2) && htRM.get(trans).get(trans2) == Relation.DIRECTCAUSAL && 
    				htRM.get(trans2).get(trans) != Relation.INVERSEDIRECTCAUSAL
    				&& htRM.get(trans2).get(trans) != Relation.DIRECTCAUSAL)
    			htRM.get(trans2).put(trans, Relation.INVERSEDIRECTCAUSAL);
    		
    		else if(!trans.equals(trans2) && htRM.get(trans).get(trans2) == Relation.IMPLICTCAUSAL && htRM.get(trans2).get(trans) != Relation.INVERSEIMPLICTCAUSUAL)
    			htRM.get(trans2).put(trans, Relation.INVERSEIMPLICTCAUSUAL);
    		
    			
    	}
    }
      
      
      //8.2New deduce <> from directcaucal 
        for(Transition trans: arTrans)            
        
            for(Transition succ: arTrans)
            {
                //two transition can reach 
                if(!trans.equals(succ) && htRM.get(succ).get(trans) == Relation.DIRECTCAUSAL && htRM.get(trans).get(succ) == Relation.DIRECTCAUSAL)
                {
                        htRM.get(succ).put(trans, Relation.L2LOOP);
                        htRM.get(trans).put(succ, Relation.L2LOOP);
                 
                }
            }
        
	
        
        
        //9: remove the row and columns of silent transitions
        for(Transition trans: arTrans)
        {
            //remove all the columns corresponding to silent transitions
            for(Transition silent: silentTransitons)
                htRM.get(trans).remove(silent);
        }
        //remove all the rows corresponding to silent transitions
        for(Transition silent: silentTransitons)
            htRM.remove(silent);
        
    }

    //find |-> in non-free-choice
    private void fixWrongDirectCausal(NetSystem net, HashMap<Transition, HashMap<Transition, Integer>> htRM, HashMap<Transition, ArrayList<Transition>> htConcurrM, HashMap<Transition, ArrayList<Transition>> htCausalM, Place[] arPlaces)
    {
        for(Place p: arPlaces)
        {
            Transition[] alInput = htPreOfPlace.get(net).get(p);
            Transition[] alOutput = htPostOfPlace.get(net).get(p);
            //must not be source place or sink place
            if(alInput.length ==0 || alOutput.length == 0)
                continue;

            for(Transition pred: alInput)
            {
                //must not loop back
                if (htLastNodes.get(net).contains(pred))
                    continue;

                for (Transition succ: alOutput)
                {
                	
                    Place[] alIn = htPreOfTrans.get(net).get(succ);
                    //must have more than one input place
                    if(alIn.length == 1)
                        continue;

                    //whether each place is pred's output or the token is already there
                    boolean isDirect = true;
                    for(Place in: alIn)
                    {
                        if (in == p)
                            continue;

                        //check whether in is one of pred's outputs
                        if(net.getDirectSuccessors(pred).contains(in))
                            continue;

                        //check whether the token is already there
                        boolean isThere = false;
                        Transition[] alPred = htPreOfPlace.get(net).get(in);
                        for(Transition tPred: alPred)
                        {
                            //tPred||pred or tPred-->pred or pPred->>pred
                            if(htConcurrM.get(pred).contains(tPred) || htCausalM.get(tPred).contains(pred))
                            {
                                isThere = true;
                                break;
                            }
                        }

                        if(!isThere)
                        {
                            isDirect = false;
                            break;
                        }
                    }

                    //fix the wrong --> to |->
                    if(!isDirect){
                        htRM.get(pred).put(succ, Relation.IMPLICTCAUSAL);
                        System.out.println(pred.getLabel()+":h:"+succ.getLabel() );
                    }
                    }
                }
            }
        
        
        }
    
   
    private boolean backwardSilent(NetSystem net, HashMap<Transition, HashMap<Transition, Integer>> htRM, Transition transPred, Transition transSucc)
    {
        //check whether transPred --> transSucc
        Place[] arInput = htPreOfTrans.get(net).get(transSucc);
        //check each input place of transSucc
        boolean isDirect = true;
        //鍒╃敤鍝堝笇琛ㄨ褰曟槸鍚﹀彉杩乼ransPred鍒板簱鎵�inputPlace鍙揪锛屽皾璇曞姞閫�?
        for(Place placeInput: arInput)
        {
            //backward recursively from this place to meet transPred
            Transition[] arInputTrans = htPreOfPlace.get(net).get(placeInput);
            //just check whether placeInput can reach transPred
            boolean isFound = false;
            ArrayList<Transition> alTempSilent = new ArrayList();
            for(Transition inputTrans: arInputTrans)
            {
                //parallel or itself is one condition to guarantee reachability
                Integer rel = htRM.get(inputTrans).get(transPred);
                if(transPred == inputTrans ||
                        rel == Relation.CONCURRENCY ||
                        //added for non-free-choice net
                        rel == Relation.DIRECTCAUSAL || rel == Relation.REACHABLE)
                {
                    isFound = true;
                    break;
                }
                //backup all silent transitions for backward attempt
                if(isSilentTrans(inputTrans))
                    alTempSilent.add(inputTrans);
            }

            //try to find a silent path backward
            if(!isFound && alTempSilent.size()>0)
            {
                //backward each silent transition recursively
                for(Transition transSilent: alTempSilent)
                {
                    isFound = backwardSilent(net, htRM, transPred, transSilent);

                    //have found one silent path, stop backward
                    if(isFound)
                        break;
                }
            }

            //stop backward because one place cannot be reached
            if(!isFound)
            {
                isDirect = false;
                break;
            }
        }

        return isDirect;
    }

    private ArrayList<Transition> getSilentPred(NetSystem net, Transition silentTransition)
    {
        ArrayList<Transition> alPred = new ArrayList();
        Place[] arInput = htPreOfTrans.get(net).get(silentTransition);
        for(Place place: arInput)
        {
            Transition[] arPred = htPreOfPlace.get(net).get(place);
            for(Transition trans: arPred)
                if(!isSilentTrans(trans) && !alPred.contains(trans))
                    alPred.add(trans);
        }

        return alPred;
    }

    private ArrayList<Transition> getSilentSucc(NetSystem net, Transition silentTransition)
    {
        ArrayList<Transition> alSucc = new ArrayList();
        Place[] arOutput = htPostOfTrans.get(net).get(silentTransition);
        for(Place place: arOutput)
        {
            Transition[] arSucc = htPostOfPlace.get(net).get(place);
            for(Transition trans: arSucc)
                if(!isSilentTrans(trans) && !alSucc.contains(trans))
                    alSucc.add(trans);
        }

        return alSucc;
    }

    private ArrayList<Transition> getSilents(NetSystem net, Transition silentTransition, Transition silentTransition2)
    {
        ArrayList<Transition> alSucc = new ArrayList();
        Place[] arOutput = htPostOfTrans.get(net).get(silentTransition);
        Place[] arOutput2 = htPostOfTrans.get(net).get(silentTransition2);
        for(Place place: arOutput)
        {
            Transition[] arSucc = htPostOfPlace.get(net).get(place);
            for(Transition trans: arSucc)
                if(isSilentTrans(trans) && !alSucc.contains(trans))
                    alSucc.add(trans);
        }

        for(Place place: arOutput)
        {
            Transition[] arSucc = htPostOfPlace.get(net).get(place);
            for(Transition trans: arSucc)
                if(isSilentTrans(trans) && !alSucc.contains(trans))
                    alSucc.add(trans);
        }
        return alSucc;
    }
    private void backwardSilent(NetSystem net, Transition silentTrans, ArrayList<ArrayList<Transition>> alSilentPaths)
    {
        //mark this silent transition as visited
        silentTrans.setTag(null);

        //try to add a silent transition to each silent path
        for(ArrayList<Transition> alSilentPath: alSilentPaths)
            alSilentPath.add(0, silentTrans);
        //clone the current silent paths
        ArrayList<ArrayList<Transition>> alSPBackup = cloneSilentPaths(alSilentPaths);
        //cleanup the silent paths for new ones
        alSilentPaths.clear();

        //backward all places
        Place[] arInput = htPreOfTrans.get(net).get(silentTrans);
        for(Place inputPlace: arInput)
        {
            //backward all transitions
            Transition[] arPred = htPreOfPlace.get(net).get(inputPlace);
            System.out.println("length:"+arPred.length);
            for(Transition pred: arPred)
            {
            	System.out.println("label:"+pred.getLabel());
            	
                //check whether it is a silent transition
                if(isSilentTrans(pred))
                {
                    //clone silent paths for recursion
                    ArrayList<ArrayList<Transition>> alSPClone = cloneSilentPaths(alSPBackup);
                    //extend the silent paths backward recursively
                    backwardSilent(net, pred, alSPClone);
                    //store the extended silent paths
                    alSilentPaths.addAll(alSPClone);
                }
            }
        }

        //backward to visible transitions now
        if(alSilentPaths.size() == 0)
            alSilentPaths.addAll(alSPBackup);
    }

    private void forwardSilent(NetSystem net, Transition silentTrans, ArrayList<ArrayList<Transition>> alSilentPaths)
    {
        //mark this silent transition as visited
        silentTrans.setTag(null);

        //try to add a silent transition to each silent path
        for(ArrayList<Transition> alSilentPath: alSilentPaths)
            alSilentPath.add(silentTrans);
        //clone the current silent paths
        ArrayList<ArrayList<Transition>> alSPBackup = cloneSilentPaths(alSilentPaths);
        //cleanup the silent paths for new ones
        alSilentPaths.clear();

        //forward all places
        Place[] arOutput = htPostOfTrans.get(net).get(silentTrans);
        for(Place outputPlace: arOutput)
        {
            //forward all transitions
            Transition[] arSucc = htPostOfPlace.get(net).get(outputPlace);
            for(Transition succ: arSucc)
            {
                //check whether it is a silent transition
                if(isSilentTrans(succ))
                {
                    //clone silent paths for recursion
                    ArrayList<ArrayList<Transition>> alSPClone = cloneSilentPaths(alSPBackup);
                    //extend the silent paths forward recursively
                    forwardSilent(net, succ, alSPClone);
                    //store the extended silent paths
                    alSilentPaths.addAll(alSPClone);
                }
            }
        }

        //forward to visible transitions now
        if(alSilentPaths.size() == 0)
            alSilentPaths.addAll(alSPBackup);
    }

    private ArrayList<ArrayList<Transition>> cloneSilentPaths(ArrayList<ArrayList<Transition>> alSilentPaths)
    {
        ArrayList<ArrayList<Transition>> alSilentClone = new ArrayList();
        for(ArrayList<Transition> alSilentPath: alSilentPaths)
            alSilentClone.add((ArrayList<Transition>)alSilentPath.clone());
        return alSilentClone;
    }

    private boolean isSilentTrans(Transition trans){    	
    	return silentNames.contains(trans.getLabel().split("-")[0]);
    }

    private void deduceConcurrs(NetSystem net, HashMap<Transition, HashMap<Transition, Integer>> htRM, HashMap<NetSystem, HashMap<Transition, Place[]>> htTrans, HashMap<NetSystem, HashMap<Place, Transition[]>> htPlace, HashMap<Transition, ArrayList<Transition>> htConcurrM, Transition trans)
    {
        //check for all the input places
        Place[] arInput = htTrans.get(net).get(trans);
        for(int i=0; i<arInput.length; i++)
        {
            //get all predecessors of arInput[i];
            Transition[] arPredI = htPlace.get(net).get(arInput[i]);
            for(int j=i+1; j<arInput.length; j++)
            {
                //get all predecessors of arInput[j]
                Transition[] arPredJ = htPlace.get(net).get(arInput[j]);
                //check if they are in parallel
                for(Transition transI: arPredI)
                {
                    HashMap<Transition, Integer> htRelI = htRM.get(transI);
                    for(Transition transJ: arPredJ)
                    {
                        HashMap<Transition, Integer> htRelJ = htRM.get(transJ);
                        //they should not be the same
                        if(transI != transJ && htRelI.get(transJ) == null && htRelJ.get(transI) == null)
                        {
                            htRelI.put(transJ, Relation.CONCURRENCY);
                            htRelJ.put(transI, Relation.CONCURRENCY);
                            //store the || relation between transI and transJ
                            htConcurrM.get(transI).add(transJ);
                            htConcurrM.get(transJ).add(transI);
                        }
                    }
                }
            }
        }
    }

    private void deduceCons(HashMap<Transition, HashMap<Transition, Integer>> htRM, HashMap<Transition, ArrayList<Transition>> htCausalM, HashMap<Transition, ArrayList<Transition>> htConM, Transition[] arTrans, Integer tr)
    {
        boolean isChanged = true;
        while(isChanged)
        {
            isChanged = false;
            for (Transition pred : arTrans)
            {
                //enumerating each pred's conflicts
                HashMap<Transition, Integer> htPred = htRM.get(pred);
                //all transitions having # or || with pred
                ArrayList<Transition> alCurr = htConM.get(pred);
                for(int i=alCurr.size()-1; i>=0; i--)
                {
                    //get the curr transition
                    Transition curr = alCurr.get(i);
                    //all transitions having --> or ->> with curr
                    ArrayList<Transition> alSucc = htCausalM.get(curr);
                    for(Transition succ: alSucc)
                    {
                        //check if a new # or || is possible
                        if (htPred.get(succ) == null)
                        {
                            isChanged = true;
                            htPred.put(succ, tr);
                            htRM.get(succ).put(pred, tr);
                            //store # or || between pred and succ
                            alCurr.add(succ);
                            htConM.get(succ).add(pred);
                        }
                    }
                }
            }
        }
    }

    private void deduceIndirectCausals(HashMap<Transition, HashMap<Transition, Integer>> htRM, HashMap<Transition, ArrayList<Transition>> htCausalM, Transition[] arTrans,
    								   HashMap<Transition, ArrayList<Transition>> htIndiect)
    {
        boolean isChanged = true;
        while(isChanged)
        {
            isChanged = false;
            for (Transition pred : arTrans)
            {
                //enumerating each pred's successors
                HashMap<Transition, Integer> htPred = htRM.get(pred);
                //all transitions having --> or ->> with pred
                ArrayList<Transition> alCurr = htCausalM.get(pred);
               // System.out.println(pred+":pred:");
                for(int i=alCurr.size()-1; i>=0; i--)
                {
                    //get the curr transition
                    Transition curr = alCurr.get(i);
                    //all transitions having --> or ->> with curr
                    ArrayList<Transition> alSucc = htCausalM.get(curr);
                    for(Transition succ: alSucc)
                    {
                        //check if a new ->> is possible
                        if (htPred.get(succ)== null )
                        {
                            isChanged = true;
                            htPred.put(succ, Relation.REACHABLE);
                            if(!htIndiect.containsKey(pred))
                            	htIndiect.put(pred, new ArrayList<Transition>());
                            
                            htIndiect.get(pred).add(succ);
                            //store --> between pred and succ
                            alCurr.add(succ);
                        }
                    }
                }
            }
        }
    }

    private void addSucc(NetSystem net, ArrayList<Node> queue, Node curr)
    {
        queue.remove(curr);
        //add all its successors to the queue
        Node[] arPost = htPostsets.get(net).get(curr);
        for(Node succ: arPost)
        {
            //if curr is the last transition of a loop
            if(curr instanceof Transition && htLastNodes.get(net).contains(curr))
                continue;
            //add succ to queue for traversal
            if(!queue.contains(succ))
                queue.add(succ);
        }
    }
    
    public HashMap<String, HashMap<String, Integer>> convertOneValue(HashMap<String, String> names, HashMap<String, HashMap<String, Integer>> map){
    	HashMap<String, HashMap<String, Integer>> relationMatrix = new HashMap<String, HashMap<String, Integer>>();
    	Iterator iterator = map.entrySet().iterator();
    	while(iterator.hasNext()){
    		Map.Entry entry = (Map.Entry)iterator.next();
    		String key = (String)entry.getKey();
      		
    		HashMap<String, Integer> values = (HashMap<String, Integer>)entry.getValue();
    		HashMap<String, Integer> newValues = new HashMap<String, Integer>();
    		
    		Iterator iterator2 = values.entrySet().iterator();
    		while(iterator2.hasNext()){
    			Map.Entry entry2 = (Map.Entry)iterator2.next();
    			String key2 = (String)entry2.getKey();
    			int value2 = (int)entry2.getValue();
    			//System.out.println(value2);
    			newValues.put(names.get(key2), convertMulti2One(value2));	
    		}
    		
    		relationMatrix.put(names.get(key), newValues);
    	}
    	return relationMatrix;
    }
    
    public  HashMap<String, HashMap<String, Integer>> getRelationMatrix(HashMap<String, String> mappingNames, NetSystem net){
    	extractFeatures(net);
    	
        HashMap<String, HashMap<String, Integer>> htRM_P = htMatricesTrans.get(net);
        return convertOneValue(mappingNames,htRM_P);        
    }
    /**
     * 
     * initialize, numberingNodes, deduceRelationMatrix must be called first
     * @param netP
     * @param netQ
     * @return the similarity betwen netP and netQ
     */
    public float similarity(NetSystem netP, NetSystem netQ)
    {
        float result = 0.0f;

        //get the relation matrix of P
        HashMap<Transition, HashMap<Transition, Integer>> htRM_P = htMatrices.get(netP);
        //get the relation matrix of Q
        HashMap<Transition, HashMap<Transition, Integer>> htRM_Q = htMatrices.get(netQ);
        
        
        printMatrixT(htRM_P);
        System.out.print("_________________________________________");
        printMatrixT(htRM_Q);
      //  HashMap<String, HashMap<Transition, Integer>> test_p = htMatrices.get 
        
        //1.1: calculate the intersection of transitions of P and Q
        HashMap<String, Transition> htTransP = new HashMap();
        Transition[] arTransP = htTransitions.get(netP);
        Transition[] arTransQ = htTransitions.get(netQ);
        //store all transitions of P in a hashmap
        for(Transition transP: arTransP)
            htTransP.put(transP.getLabel(), transP);
        //store the mapping of the transitions from P to Q
        HashMap<Transition, Transition> hmPtoQ = new HashMap();
        for(Transition transQ: arTransQ)
        {
            //should not be a silent transition
            if(isSilentTrans(transQ))
                continue;
            Transition transP = htTransP.get(transQ.getLabel());
            if(transP != null)
                hmPtoQ.put(transP, transQ);
        }

        //1.2: calculate the intersection of RM_P and RM_Q
/*        Set<Transition> setCommon = hmPtoQ.keySet();
        for(Transition transP: setCommon)
        {
            for(Transition transQ: setCommon)
            {
                int row = htRM_P.get(transP).get(transQ);
                row = (int)(Math.log(row)/Math.log(2));
                int col = htRM_Q.get(hmPtoQ.get(transP)).get(hmPtoQ.get(transQ));
                col = (int)(Math.log(col)/Math.log(2));
                result += TaskRelation.WEIGHTOFRELATIONS[row][col];
            }
        }

        //1.3: calculate the similarity of P and Q
      //  result = result/(htRM_P.size()*htRM_P.size() + htRM_Q.size()*htRM_Q.size() - result);*/

        return result;
    }

    private void extractFeatures(NetSystem net)
    {
        initialize(net);
        numberingNodes(net, false);
        deduceRelationMatrix(net);
        foldEvents2Trans(net);
    }

    /**
     * calculate the similarity between two give net systems
     * @param netP
     * @param netQ
     * @return
     */
    public float simTrans(NetSystem netP, NetSystem netQ)
    {
        extractFeatures(netP);
        extractFeatures(netQ);

        //get the relation matrix of P
        HashMap<String, HashMap<String, Integer>> htRM_P = htMatricesTrans.get(netP);
        //get the relation matrix of Q
        HashMap<String, HashMap<String, Integer>> htRM_Q = htMatricesTrans.get(netQ);
//
//        System.out.println("--------------------------------------");
//        printMatrix(htRM_P);
//        System.out.println("-");
//         
        
        
        //1.1: calculate the intersection of transitions of P and Q
        HashMap<String, String> htTransP = new HashMap();
        //store all transitions of P in a hashmap
        for(String transP: htRM_P.keySet())
            htTransP.put(transP, transP);
        //store the mapping of the transitions from P to Q
        HashMap<String, String> hmPtoQ = new HashMap();
        for(String transQ: htRM_Q.keySet())
        {
            String transP = htTransP.get(transQ);
            if(transP != null)
                hmPtoQ.put(transP, transQ);
        }

        float result = 0.0f;
     
        return result;
    }
    
    public void printMatrix(HashMap<String, String> names, HashMap<String, HashMap<String, Integer>> map){    	
    	Iterator iterator = map.entrySet().iterator();
    	while(iterator.hasNext()){
    		Map.Entry entry = (Map.Entry)iterator.next();
    		String key = (String)entry.getKey();
    		System.out.println(key);
    	
    		System.out.print(String.format("%7s", "["+names.get(key)+"]" + "\t"));
    		
    		HashMap<String, Integer> values = (HashMap<String, Integer>)entry.getValue();
    		
    		Iterator iterator2 = values.entrySet().iterator();
    		while(iterator2.hasNext()){
    			Map.Entry entry2 = (Map.Entry)iterator2.next();
    			String key2 = (String)entry2.getKey();
    			int value2 = (int)entry2.getValue();
    			//System.out.println(value2);
    			System.out.print(names.get(key2) + ":"+ value2 + "\t");
    			
    		}
    		
    		System.out.println();
    		
    	}
    }
    
   
    
    
    public static  void printMatrixT( HashMap<Transition, HashMap<Transition, Integer>> htRM_P){
    	Iterator iterator = htRM_P.entrySet().iterator();
    	while(iterator.hasNext()){
    		Map.Entry entry = (Map.Entry)iterator.next();
    		Transition key = (Transition)entry.getKey();
    		System.out.print(String.format("%7s", "["+key.getLabel()+"]" + "\t"));
    		
    		HashMap<String, Integer> values = (HashMap<String, Integer>)entry.getValue();
    		
    		Iterator iterator2 = values.entrySet().iterator();
    		while(iterator2.hasNext()){
    			Map.Entry entry2 = (Map.Entry)iterator2.next();
    			Transition key2 = (Transition)entry2.getKey();
    			Integer value2 = (Integer)entry2.getValue();
    			
    			System.out.print(key2.getLabel() + ":"+ value2 + "\t");
    		}
    		
    		System.out.println();
    		
    	}
    }
    
    //transfer multi-relation to only one relation
    private int convertMulti2One(int rel)
    {
    	/*if(rel > Relation.L2LOOP && rel < Relation.DIRECTCAUSAL)
    		rel = Relation.L2LOOP;
    	else if(rel > Relation.DIRECTCAUSAL && rel < Relation.INVERSEDIRECTCAUSAL)
            rel = Relation.DIRECTCAUSAL;
    	
    	else if(rel > Relation.INVERSEDIRECTCAUSAL && rel < Relation.INDIRECTCAUSAL)
            rel = Relation.INVERSEDIRECTCAUSAL;
    	
    	else if(rel > Relation.INDIRECTCAUSAL && rel < Relation.INVERSEINDIRECTCAUSAL)
            rel = Relation.INDIRECTCAUSAL;
    	
    	else if(rel > Relation.INVERSEINDIRECTCAUSAL && rel < Relation.IMPLICTCAUSAL)
            rel = Relation.INVERSEINDIRECTCAUSAL;
    	
        else if(rel > Relation.IMPLICTCAUSAL && rel < Relation.INVERSEIMPLICTCAUSUAL)
            rel = Relation.IMPLICTCAUSAL;
    	
        else if(rel > Relation.INVERSEIMPLICTCAUSUAL && rel < Relation.CONCURRENCY)
            rel = Relation.INVERSEIMPLICTCAUSUAL;
    	
        else if(rel > Relation.CONCURRENCY && rel < Relation.CONFLICT)
            rel = Relation.CONCURRENCY;
    	
        else if(rel > Relation.CONFLICT)
            rel = Relation.CONFLICT;
    	*/
    	if(rel > Relation.CONFLICT)
    		rel = rel - Relation.CONFLICT;
    
    	return rel;
    }

    /**
     * convert relations between events in CPU to transitions in net
     * @param net
     */
    public void foldEvents2Trans(NetSystem net)
    {
        htMatricesTrans.remove(net);

        //store the relations between transitions for later usage
        HashMap<String, HashMap<String, Integer>> hmRM = new HashMap();
        htMatricesTrans.put(net, hmRM);

        HashMap<Transition, HashMap<Transition, Integer>> htRM = htMatrices.get(net);
        //enumerate all the relations between events to fold them
        for(Transition pred: htRM.keySet())
        {
            HashMap<Transition, Integer> htRel = htRM.get(pred);
            String strPred = getName(pred);
            HashMap<String, Integer> hmRel = hmRM.get(strPred);
            if(hmRel == null)
            {
                hmRel = new HashMap();
                hmRM.put(strPred, hmRel);
            }
            for(Transition succ: htRel.keySet())
            {
                Integer nRelE = htRel.get(succ);
                //fold this relation between events to transitions
                String strSucc = getName(succ);
                Integer nRelT = hmRel.get(strSucc);
                if(nRelT == null)
                    nRelT = 0;

                //fold the relation between pred and succ to their transitions
                nRelT = nRelT | nRelE;
                //store the new relation
                hmRel.put(strSucc, nRelT);
            }
        }

//        System.out.println(htMatricesTrans.get(net));
    }

    private static String getName(Transition pred)
    {
        String strPred = pred.toString();
        int idx = strPred.lastIndexOf('-');
        strPred = strPred.substring(0, idx);
        return strPred;
    }

    public int[] countNumOfRels(NetSystem netP, int[] narCount)
    {
        int[] narCntOne = new int[]{0, 0, 0, 0, 0, 0, 0};
        //get the relation matrix of P
        HashMap<Transition, HashMap<Transition, Integer>> htRM_P = htMatrices.get(netP);

        //count the number of relations between transP and transQ
        Set<Transition> setCommon = htRM_P.keySet();
        for(Transition transP: setCommon)
        {
            for(Transition transQ: setCommon)
            {
                int row = htRM_P.get(transP).get(transQ);

                row = (int)(Math.log(row)/Math.log(2));
                narCount[row]++;
                narCntOne[row]++;

//                if(row == TaskRelation.DIRECTCAUSAL)
//                {
//                    narCount[0]++;
//                    narCntOne[0]++;
//                }
//                else if(row == TaskRelation.INVERSEDIRECTCAUSAL)
//                {
//                    narCount[1]++;
//                    narCntOne[1]++;
//                }
//                else if(row == TaskRelation.INDIRECTCAUSAL)
//                {
//                    narCount[2]++;
//                    narCntOne[2]++;
//                }
//                else if(row == TaskRelation.INVERSEINDIRECTCAUSAL)
//                {
//                    narCount[3]++;
//                    narCntOne[3]++;
//                }
//                else if(row == TaskRelation.ALWAYSCONCURRENCY)
//                {
//                    narCount[4]++;
//                    narCntOne[4]++;
//                }
//                else if(row == TaskRelation.SOMETIMESCONCURRENCY)
//                {
//                    narCount[5]++;
//                    narCntOne[5]++;
//                }
//                else if(row == TaskRelation.CONFLICT)
//                {
//                    narCount[6]++;
//                    narCntOne[6]++;
//                }
            }
        }

//        System.out.println("=== One number of relations of BP+ ===");
//        for(int n : narCntOne)
//            System.out.println(n);
        return narCntOne;
    }

    private static int[] cntNumOfRelsComp(NetSystem netP, int[] nTotalRelBP, int[] narCau2All, int[] narRev2All, int[] narPar2All, int[] narExc2All, HashMap<Transition, HashMap<Transition, Integer>> htRM)
    {
        int[] narCntOne = new int[]{0, 0, 0, 0};
        BehaviouralProfile<NetSystem, Node> bp = BehavioralProfileSimilarity.getBP(netP);

        //mapping between nodes
        List<Node> alNodes = bp.getEntities();
        Set<Transition> alTrans = htRM.keySet();

        //set mapping between the nodes for BP and BP+
        HashMap<String, Transition> hmBPP = new HashMap();
        for(Transition trans: alTrans)
            hmBPP.put(trans.getId(), trans);

        for(Node n1: alNodes)
        {
            Transition trans1 = hmBPP.get(n1.getId());
            for(Node n2: alNodes)
            {
                Transition trans2 = hmBPP.get(n2.getId());
                int rel = htRM.get(trans1).get(trans2);

                RelSetType rst = bp.getRelationForEntities(n1, n2);
                if(rst == RelSetType.Interleaving)
                {
                    if(rel == Relation.DIRECTCAUSAL)
                        narPar2All[0]++;
                    else if(rel == Relation.REACHABLE)
                        narPar2All[1]++;
                    else if(rel == Relation.INVERSEDIRECTCAUSAL)
                        narPar2All[2]++;
                    else if(rel == Relation.INVERSEREACHABLE)
                        narPar2All[3]++;
                    else if(rel == Relation.CONCURRENCY)
                        narPar2All[4]++;
                    else if(rel == Relation.CONCURRENCY)
                        narPar2All[5]++;
                    else if(rel == Relation.CONFLICT)
                        narPar2All[6]++;
                }
                else if(rst == RelSetType.Order)
                {
                    if(rel == Relation.DIRECTCAUSAL)
                        narCau2All[0]++;
                    else if(rel == Relation.REACHABLE)
                        narCau2All[1]++;
                    else if(rel == Relation.CONFLICT)
                        narCau2All[2]++;
                    else
                        narCau2All[3]++;
                }
                else if(rst == RelSetType.ReverseOrder)
                {
                    if(rel == Relation.INVERSEDIRECTCAUSAL)
                        narRev2All[0]++;
                    else if(rel == Relation.INVERSEREACHABLE)
                        narRev2All[1]++;
                    else if(rel == Relation.CONFLICT)
                        narRev2All[2]++;
                    else
                        narRev2All[3]++;
                }
                else if(rst == RelSetType.Exclusive)
                {
                    if(rel == Relation.CONFLICT)
                        narExc2All[0]++;
                    else
                        narExc2All[1]++;
                }
            }
        }

        RelSetType[][] arRels = bp.getMatrix();
        for(int m = 0; m < arRels.length; m++)
        {
            for (int n = 0; n < arRels[m].length; n++)
            {
                RelSetType rst = arRels[m][n];
                if(rst == RelSetType.Order)
                {
                    nTotalRelBP[0]++;
                    narCntOne[0]++;
                }
                else if(rst == RelSetType.ReverseOrder)
                {
                    nTotalRelBP[1]++;
                    narCntOne[1]++;
                }
                else if(rst == RelSetType.Interleaving)
                {
                    nTotalRelBP[2]++;
                    narCntOne[2]++;
                }
                else if(rst == RelSetType.Exclusive)
                {
                    nTotalRelBP[3]++;
                    narCntOne[3]++;
                }
            }
        }

        return narCntOne;
    }

    private static int[] countNumOfRelsBP(NetSystem netP, int[] nTotalRelBP)
    {
        int[] narCntOne = new int[]{0, 0, 0, 0};
        BehaviouralProfile<NetSystem, Node> bp = BehavioralProfileSimilarity.getBP(netP);
        RelSetType[][] arRels = bp.getMatrix();
        for(int m = 0; m < arRels.length; m++)
        {
            for (int n = 0; n < arRels[m].length; n++)
            {
                RelSetType rst = arRels[m][n];
                if(rst == RelSetType.Order)
                {
                    nTotalRelBP[0]++;
                    narCntOne[0]++;
                }
                else if(rst == RelSetType.ReverseOrder)
                {
                    nTotalRelBP[1]++;
                    narCntOne[1]++;
                }
                else if(rst == RelSetType.Interleaving)
                {
                    nTotalRelBP[2]++;
                    narCntOne[2]++;
                }
                else if(rst == RelSetType.Exclusive)
                {
                    nTotalRelBP[3]++;
                    narCntOne[3]++;
                }
            }
        }

//        System.out.println("=== One number of relations of BP ===");
//        for(int n : narCntOne)
//            System.out.println(n);
        return narCntOne;
    }

    public static void main11(String[] args)
    {
        PNMLSerializer pnmlSerializer = new PNMLSerializer();

        //load the models one by one in a given folder
        File folder = new File("models");
        File[] arModels = folder.listFiles(new FileUtil().getFileter("pnml"));

        int[] nTotalRels = new int[]{0, 0, 0, 0, 0, 0, 0}; //--> <-- ->> <<- || ||- #
        int[] nTotalRelBP = new int[]{0, 0, 0, 0};  //-> <- || +
        int[] nTotalC2A = new int[]{0, 0, 0, 0};  //--> ->> # unknown
        int[] nTotalR2A = new int[]{0, 0, 0, 0};  //<-- <<- # unknown
        int[] nTotalP2C = new int[]{0, 0, 0, 0, 0, 0, 0};  //--> ->> <-- <<- || ||- #
        int[] nTotalE2C = new int[]{0, 0};  //# unknown
        int nFailed = 0;
        for(int i=0; i<arModels.length; i++)
        {
            String fpModelP = arModels[i].getName();
            System.out.println(fpModelP);

            String filepathP = arModels[i].getAbsolutePath();
            NetSystem netP = pnmlSerializer.parse(filepathP);

            BPPlusSimilarity bpp = new BPPlusSimilarity();

            bpp.initialize(netP);
            bpp.numberingNodes(netP, false);
            bpp.deduceRelationMatrix(netP);

            int[] narCntOneBPP = bpp.countNumOfRels(netP, nTotalRels);

            //int[] narCntOneBP = countNumOfRelsBP(netP, nTotalRelBP);
            int[] narCau2All = new int[]{0, 0, 0, 0};
            int[] narRev2All = new int[]{0, 0, 0, 0};
            int[] narPar2All = new int[]{0, 0, 0, 0, 0, 0, 0};
            int[] narExc2All = new int[]{0, 0};
            int[] narCntOneBP = cntNumOfRelsComp(netP, nTotalRelBP, narCau2All, narRev2All, narPar2All, narExc2All, bpp.htMatrices.get(netP));

            //whether the equations about the numbers are correct?
            boolean isCorrect = true;
            if(narCntOneBPP[4] != narPar2All[4])
                System.out.println("========== || error ==========");

            //-> ==> --> and ->> and # and unknown
            for(int k=0; k<narCau2All.length; k++)
                nTotalC2A[k] += narCau2All[k];

            //<- ==> <-- and <<- and # and unknown
            for(int k=0; k<narRev2All.length; k++)
                nTotalR2A[k] += narRev2All[k];

            //|| ==> --> and ->> and || ||- and #
            for(int k=0; k<narPar2All.length; k++)
                nTotalP2C[k] += narPar2All[k];

            //+ ==> # and unknown
            for(int k=0; k<narExc2All.length; k++)
                nTotalE2C[k] += narExc2All[k];

            //# < +
            if(narCntOneBPP[6] < narCntOneBP[3])
            {
                nFailed++;
                System.out.println("===1=== BP+: " + narCntOneBPP[6] + " === BP: " + narCntOneBP[3] + " ======");
                arModels[i].renameTo(new File("models/efficiency/" + fpModelP));
            }

            //<-- + <<- > <-
            if(narCntOneBPP[1] + narCntOneBPP[3] > narCntOneBP[1])
            {
                nFailed++;
                System.out.println("===2=== BP+: " + (narCntOneBPP[1] + narCntOneBPP[3]) + " === BP: " + narCntOneBP[1] + " ======");
                arModels[i].renameTo(new File("models/efficiency/" + fpModelP));
            }
        }

        System.out.println("=== Total number of relations of BP+ ===");
        for(int n : nTotalRels)
            System.out.println(n);

        System.out.println("=== Total number of relations of BP ===");
        for(int n : nTotalRelBP)
            System.out.println(n);

        System.out.println("=== The number of non-sound models ===");
        System.out.println(nFailed);

        System.out.println("=== Transformation of -> to BP+ ===");
        System.out.println("-> ==> -->: " + nTotalC2A[0]);
        System.out.println("-> ==> ->>: " + nTotalC2A[1]);
        System.out.println("-> ==> #  : " + nTotalC2A[2]);
        System.out.println("-> ==>    : " + nTotalC2A[3]);

        System.out.println("=== Transformation of <- to BP+ ===");
        System.out.println("<- ==> <--: " + nTotalR2A[0]);
        System.out.println("<- ==> <<-: " + nTotalR2A[1]);
        System.out.println("<- ==> #  : " + nTotalR2A[2]);
        System.out.println("<- ==>    : " + nTotalR2A[3]);

        System.out.println("=== Transformation of || to BP+ ===");
        System.out.println("|| ==> -->: " + nTotalP2C[0]);
        System.out.println("|| ==> ->>: " + nTotalP2C[1]);
        System.out.println("|| ==> <--: " + nTotalP2C[2]);
        System.out.println("|| ==> <<-: " + nTotalP2C[3]);
        System.out.println("|| ==> || : " + nTotalP2C[4]);
        System.out.println("|| ==> ||-: " + nTotalP2C[5]);
        System.out.println("|| ==> #  : " + nTotalP2C[6]);

        System.out.println("=== Transformation of + to BP+ ===");
        System.out.println("+ ==> # : " + nTotalE2C[0]);
        System.out.println("+ ==>   : " + nTotalE2C[1]);
    }

    //compare BP to BP+
    public static void main(String[] args)
    {
        PNMLSerializer pnmlSerializer = new PNMLSerializer();

        //load the models one by one in a given folder
        File folder = new File("models");
        File[] arModels = folder.listFiles(new FileUtil().getFileter("pnml"));
        System.out.println(arModels.length);
        for(int i=0; i<arModels.length; i++)
        {
            String fpModelP = arModels[i].getName();
            String filepathP = arModels[i].getAbsolutePath();
            NetSystem netP = pnmlSerializer.parse(filepathP);

           for (int j = i; j < arModels.length; j++)
            {
                String fpModelQ = arModels[j].getName();
                String filepathQ = arModels[j].getAbsolutePath();
                NetSystem netQ = pnmlSerializer.parse(filepathQ);
		
              //  System.out.println("======= " + fpModelP + " : " + fpModelQ + " =======");
                System.out.println("========" + fpModelP + "==============");
                BPPlusSimilarity bpp = new BPPlusSimilarity();
                BehavioralProfileSimilarity bp = new BehavioralProfileSimilarity();

                bpp.initialize(netP);
                bpp.initialize(netQ);
                bpp.numberingNodes(netP, true);
                bpp.numberingNodes(netQ, true);
                bpp.deduceRelationMatrix(netP);
                bpp.deduceRelationMatrix(netQ);
                
               // float testBPP = bpp.simTrans(netP, netQ);
                float simBPP = bpp.similarity(netP, netQ);
                float simBP = bp.similarity(netP, netQ);

                System.out.println("BP+: " + simBPP);
                System.out.println("BP : " + simBP);
              //  System.out.println("test bp+ bpp:" + testBPP);
                
                System.out.println("haha.............");
                //printMatrix(bpp, netP);
               // printMatrix2(netP, bpp.htConflicts);
            }
        }
    }
    
    public static void printMatrix2(NetSystem net, HashMap<NetSystem, HashMap<Transition, ArrayList<Transition>>> item){
    	HashMap<Transition, ArrayList<Transition>> map = item.get(net);
    	Iterator iterator = map.entrySet().iterator();
    	
    	while(iterator.hasNext()){
    		Map.Entry entry = (Map.Entry)iterator.next();
    		Transition key = (Transition)entry.getKey();
    		System.out.print(String.format("%7s","[" + key.getLabel() + "]" + " "));
    		
    		ArrayList<Transition> list = (ArrayList<Transition>)entry.getValue();
    		for(int i = 0; i < list.size();i++){
    			System.out.print(list.get(i).getLabel()+"\t");
    		}
    		
    	}
    	
    }
    public static void printMatrix(BPPlusSimilarity bpp, NetSystem net){
    	HashMap<NetSystem, HashMap<Transition, HashMap<Transition, Integer>>> map = bpp.htMatrices;
    	HashMap<Transition, HashMap<Transition, Integer>> matrix = map.get(net);
    	
    	Iterator iterator = matrix.entrySet().iterator();
    	while(iterator.hasNext()){
    		Map.Entry entery = (Map.Entry)iterator.next();
    		Transition key = (Transition)entery.getKey();
    		HashMap<Transition, Integer> value = (HashMap<Transition, Integer>)entery.getValue();
    		
    		System.out.print(String.format("%7s","[" + key.getLabel() + "]" + " "));
    		
    		Iterator iterator2 = value.entrySet().iterator();
    		
    		while(iterator2.hasNext()){
    			Map.Entry entry = (Map.Entry)iterator2.next();
    			Transition tra = (Transition)entry.getKey();
    			Integer val = (Integer)entry.getValue();
    			
    			System.out.print(tra.getLabel()+":" + val +"\t");
    			//System.out.print(val + "\t");
    		}
    		
    		System.out.println();
    	}
    }
    
    //compare BP+ to BP++
    public static void mainEfficiency(String[] args)
    {
        PNMLSerializer pnmlSerializer = new PNMLSerializer();

        //load the models one by one in a given folder
        File folder = new File("models");
        File[] arModels = folder.listFiles(new FileUtil().getFileter("pnml"));

        long nTotalCostBPP = 0;
        long nTotalCostBP = 0;
        int nTotalTrans = 0;
        int nMaxTrans = 0;
        int nMinTrans = Integer.MAX_VALUE;
        int nTotalPlace = 0;
        int nMaxPlace = 0;
        int nMinPlace = Integer.MAX_VALUE;
        int nTotalArc = 0;
        int nMaxArc = 0;
        int nMinArc = Integer.MAX_VALUE;

        for(int i=0; i<arModels.length; i++)
        {
            String fpModelP = arModels[i].getName();
            String filepathP = arModels[i].getAbsolutePath();
            NetSystem netP = pnmlSerializer.parse(filepathP);

            //move it if it is a WF-net with multiple source places
//            if(netP.getSourceNodes().size() > 1)
//            {
//                arModels[i].renameTo(new File(folder + "/Not-WF-net/" + fpModelP));
//                System.out.println(fpModelP);
//
//                continue;
//            }

            //count the features of the model sets
            int nTrans = netP.getTransitions().size();
            nTotalTrans += nTrans;
            if(nTrans < nMinTrans)
                nMinTrans = nTrans;
            if(nTrans > nMaxTrans)
                nMaxTrans = nTrans;

            int nPlace = netP.getPlaces().size();
            nTotalPlace += nPlace;
            if(nPlace < nMinPlace)
                nMinPlace = nPlace;
            if(nPlace > nMaxPlace)
                nMaxPlace = nPlace;

            int nArc = netP.getEdges().size();
            nTotalArc += nArc;
            if(nArc < nMinArc)
                nMinArc = nArc;
            if(nArc > nMaxArc)
                nMaxArc = nArc;

            for (int j = i; j < i+1; j++)
            {
                String fpModelQ = arModels[j].getName();
                String filepathQ = arModels[j].getAbsolutePath();
                NetSystem netQ = pnmlSerializer.parse(filepathQ);

                System.out.println("======= " + fpModelP + " : " + fpModelQ + " =======");
                BPPlusSimilarity bpp = new BPPlusSimilarity();
                BehavioralProfileSimilarity bp = new BehavioralProfileSimilarity();

                long minBPP = Long.MAX_VALUE;
                long minBP = Long.MAX_VALUE;
                for (int n = 0; n < 1; n++)
                {
                    long before = 0, after = 0;

                    //time cost of BPP
                    before = System.nanoTime();

                    bpp.initialize(netP);
                    bpp.initialize(netQ);
                    bpp.numberingNodes(netP, false);
                    bpp.numberingNodes(netQ, false);
                    bpp.deduceRelationMatrix(netP);
                    bpp.deduceRelationMatrix(netQ);
                    bpp.similarity(netP, netQ);

                    after = System.nanoTime();
                    if (after - before < minBPP)
                        minBPP = after - before;

                    //time cost of BP
                    before = System.nanoTime();

                    bp.similarity(netP, netQ);

                    after = System.nanoTime();
                    if (after - before < minBP)
                        minBP = after - before;
                }

                //store this cost for computing the similarity of one pair
                nTotalCostBPP += minBPP;
                nTotalCostBP += minBP;

                System.out.println("BP+: " + minBPP);
                System.out.println("BP : " + minBP);
            }
        }

        System.out.println("====== Total Cost ======");
        System.out.println("Total cost of BP+: " + nTotalCostBPP);
        System.out.println("Total cost of BP : " + nTotalCostBP);
        System.out.println("nTotalTrans: " + nTotalTrans);
        System.out.println("nTotalPlace: " + nTotalPlace);
        System.out.println("nTotalArc: " + nTotalArc);

        float nAverageCostBPP = nTotalCostBPP*1.0f/arModels.length;
        float nAverageCostBP = nTotalCostBP*1.0f/arModels.length;
        System.out.println("Avg cost of BP+: " + nAverageCostBPP);
        System.out.println("Avg cost of BP : " + nAverageCostBP);
        System.out.println("Ratio of BP/BP+: " + 1.0f*nAverageCostBP/nAverageCostBPP);
        System.out.println("nTotalModels: " + arModels.length);
        System.out.println("nMaxTrans: " + nMaxTrans);
        System.out.println("nAvgTrans: " + nTotalTrans*1.0f/arModels.length);
        System.out.println("nMinTrans: " + nMinTrans);
        System.out.println("nMaxPlace: " + nMaxPlace);
        System.out.println("nAvgPlace: " + nTotalPlace*1.0f/arModels.length);
        System.out.println("nMinPlace: " + nMinPlace);
        System.out.println("nMaxArc: " + nMaxArc);
        System.out.println("nAvgArc: " + nTotalArc*1.0f/arModels.length);
        System.out.println("nMinArc: " + nMinArc);
    }
}