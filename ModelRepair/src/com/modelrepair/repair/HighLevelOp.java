package com.modelrepair.repair;

import java.util.ArrayList;
import java.util.Set;

import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Place;
import org.jbpt.petri.Transition;
import org.processmining.framework.models.petrinet.PetriNet;

import com.modelrepair.relation.Relation;
import com.modelrepair.util.FileUtil;
import com.modelrepair.util.PetriNetConversion;

public class HighLevelOp {

	public HighLevelOp() {

	}

	public static ArrayList<Transition> getSuccSilentTransitions(Place pre, NetSystem model) {
		ArrayList<Transition> silents = new ArrayList<Transition>();
		Set<Transition> silentTransitions = model.getPostset(pre);
		for (Transition t : silentTransitions) {
			if (t.getLabel().contains(Relation.SLIENTTRANSITION))
				silents.add(t);
		}
		return silents;
	}

	public static ArrayList<Transition> getPreSilentTransitions(Place succ, NetSystem model) {
		ArrayList<Transition> silents = new ArrayList<Transition>();
		Set<Transition> silentTransitions = model.getPreset(succ);
		for (Transition t : silentTransitions) {
			if (t.getLabel().contains(Relation.SLIENTTRANSITION))
				silents.add(t);
		}
		return silents;
	}

	public static ArrayList<Transition> getSourceTransitions(NetSystem model) {
		ArrayList<Transition> source = new ArrayList<Transition>();
		for (Place p : model.getPlaces()) {
			if (model.getPreset(p).size() == 0) {
				source.addAll(model.getPostset(p));
			}
		}
		return source;
	}

	public static ArrayList getSinkTransitions(NetSystem model) {
		ArrayList<Transition> sink = new ArrayList<Transition>();
		for (Place p : model.getPlaces()) {
			if (model.getPostset(p).size() == 0) {
				sink.addAll(model.getPreset(p));
			}
		}
		return sink;
	}

	public static int getPreSilenNum(Transition t, NetSystem model) {
		int num = 0;
		for (Place pre : model.getPreset(t)) {
			for (Transition ti : model.getPreset(pre)) {
				if (ti.getLabel().contains(Relation.SLIENTTRANSITION))
					num++;
			}
		}
		return num;
	}

	public static int getSuccSilenNum(Transition t, NetSystem model) {
		int num = 0;
		for (Place succ : model.getPostset(t)) {
			for (Transition ti : model.getPostset(succ)) {
				if (ti.getLabel().contains(Relation.SLIENTTRANSITION))
					num++;
			}
		}
		return num;
	}

	// delete the one transition in the model
	public static NetSystem deleteOneTransition(Transition tt, NetSystem model) {
		Set<Place> prePlace = model.getPreset(tt);
		Set<Place> succPlace = model.getPostset(tt);
		ArrayList<Transition> silentTransitions = getSilentTransitions(model);
		if (tt.getLabel().contains(Relation.SLIENTTRANSITION))
			return model;
		// 1.transition belongs to source transitions
		if (getSourceTransitions(model).contains(tt)) {
			for (Place pre : prePlace) {
				if (succPlace.size() > 1) {
					System.out.println("111111111111111111111111111111111111");
					ArrayList<Place> temp = new ArrayList<Place>();
					for (Place succ : succPlace) {
						if (model.getPostset(succ).size() == getSuccSilentTransitions(succ, model).size()) {
							deleteSilentTransition(succ, model);
							System.out.println("11111111111111111111112");
							model.removePlace(succ);
							temp.add(succ);
						}
					}

					succPlace.removeAll(temp);

					if (succPlace.size() == 0) {
						model.removePlaces(temp);
						model.removeTransitions(model.getPostsetTransitions(temp));
						model.getPostset(pre).remove(tt);
						System.out.println("111111111111111111111111111111113");
					} else {
						Transition newSilent = new Transition(Relation.SLIENTTRANSITION + "start");
						model.addFlow(pre, newSilent);
						for (Place p : succPlace) {
							model.addFlow(newSilent, p);
						}
						model.addTransition(newSilent);
						model.getPostset(pre).remove(tt);
						model.getPostset(pre).add(newSilent);
						model.getPostset(newSilent).addAll(succPlace);
						System.out.println("1111111111111111111111111111111111111111111111114");
					}
					model.removeTransition(tt);

				} else {

					if (model.getPostset(pre).size() - getSuccSilentTransitions(pre, model).size() != 1) {
						// invisible task
						System.out.println("222222222222222222222222222222222222222222222");
						model.getPostset(pre).remove(tt);
						for (Place succ : succPlace) {
							if ((model.getPreset(succ).size() - getPreSilentTransitions(succ, model).size()) >= 1) {
								// succ don't delete
								deleteSilentTransition(succ, model);
								model.getPreset(succ).remove(tt);
								System.out.println("2.11111");

							} else {
								deleteSilentTransition(succ, model);
								for (Transition t : model.getPostset(succ)) {
									for (Place p : prePlace) {
										model.addFlow(p, t);
									}

								}
								System.out.println("2.2.......");
								model.getPostset(pre).addAll(model.getPostset(succ));
								model.getPresetPlaces(model.getPostset(succ)).remove(succ);
								model.removePlace(succ);
							}

						}
					} else {
						if (getSuccSilenNum(tt, model) >= 1) {
							for (Place succ : succPlace) {
								if (model.getPostset(succ).size() == 1
										&& model.getPostsetTransitions(model.getPostsetPlaces(model.getPostset(succ)))
												.size() == 0) {
									deleteSilentTransition(succ, model);
									model.removePlace(succ);
								}
							}
						}
						System.out.println("no...............");
						deleteSilentTransition(pre, model);
						model.removePlace(pre);
					}
				}
			}
			model.removeTransition(tt);
		}

		// 2.transition belongs to sink transitions
		else if (getSinkTransitions(model).contains(tt)) {
			System.out.println(tt.getLabel());
			for (Place succ : succPlace) {

				if (prePlace.size() > 1) {
					ArrayList<Place> temp = new ArrayList<Place>();
					for (Place pre : prePlace) {
						if (model.getPreset(pre).size() == getPreSilentTransitions(pre, model).size()) {
							model.removeTransitions(model.getPreset(pre));
							model.removeTransition(tt);
							deleteSilentTransition(pre, model);
							model.removePlace(pre);
							temp.add(pre);
							System.out.println("haha........8");
							// continue;
						}
					}
					prePlace.removeAll(temp);
					if (prePlace.size() == 0) {

						model.removeTransitions(model.getPresetTransitions(temp));
						model.getPreset(succ).remove(tt);
						System.out.println("111111111111111111111111111111113");
					} else {
						System.out.println("333333333333333333333333333333333333");
						Transition newSilent = new Transition(Relation.SLIENTTRANSITION + "end");
						model.addFlow(newSilent, succ);
						for (Place p : prePlace) {
							System.out.println("new.....");
							model.addFlow(p, newSilent);
						}

						model.addTransition(newSilent);
	               					model.getPreset(succ).remove(tt);
						model.getPreset(succ).add(newSilent);
						model.getPreset(newSilent).addAll(prePlace);
					}
				} else {
					if (model.getPreset(succ).size() - getPreSilentTransitions(succ, model).size() != 1) {
						// invisible task
						model.getPreset(succ).remove(tt);
						for (Place pre : prePlace) {
							if ((model.getPostset(pre).size() - getSuccSilentTransitions(pre, model).size()) >= 1) {
								// succ don't delete
								System.out.println("4444444444444444444444444444444444444");
								deleteSilentTransition(pre, model);
								model.getPostset(pre).remove(tt);
							} else {
								System.out.println("555555555555555555555555555555555555555");
								deleteSilentTransition(succ, model);
								for (Transition t : model.getPreset(pre)) {
									for (Place p : succPlace) {
										model.addFlow(t, p);
									}
								}
								model.getPostsetPlaces(model.getPreset(pre)).remove(pre);

								model.removePlace(pre);
							}

						}
					} else {
						System.out.println("6666666666666666666666666666666666666666");
						if (getPreSilenNum(tt, model) >= 1) {
							for (Place pre : prePlace) {
								if (model.getPostset(pre).size() == 1) {
									deleteSilentTransition(pre, model);
									model.removePlace(pre);
								}
							}
						}
						deleteSilentTransition(succ, model);
						model.removePlace(succ);
					}
				}
			}
			model.removeTransition(tt);
		}
		// 3.the normal transition in the model
		else {
			if (prePlace.equals(succPlace)) {
				System.out.println("here....");
				if (model.getPostsetTransitions(prePlace).size() == 1) {
					System.out.println("nono34");
					model.removePlaces(prePlace);
					model.removePlaces(succPlace);
				}
				model.removeTransition(tt);
				return model;
			}
			for (Place pre : prePlace) {// pre do not delete
				if (model.getPresetPlaces(model.getPostsetTransitions(succPlace)).size() <= 1) {
					model.getPostset(pre).remove(tt);
			
				} else {
					System.out.println("nonono");
					Set<Transition> preSet = model.getPreset(pre);
					System.out.println(preSet.size());
					for (Transition t : preSet) {
						System.out.println("label:" + t.getLabel());
						if (t.getLabel().contains(Relation.SLIENTTRANSITION)
								&& model.getPostsetTransitions(model.getPostset(t)).size() - getSuccSilenNum(t, model) > 1
								|| (!t.getLabel().contains(Relation.SLIENTTRANSITION)
										&& model.getPostsetTransitions(model.getPostset(t)).size() - getSuccSilenNum(t, model) >= 1)) {
							System.out.println("88888888888888888");
							model.getPostset(t).addAll(succPlace);
							for (Place su : succPlace) {
								model.addFlow(t, su);
							}
							model.getPresetTransitions(succPlace).add(t);
						}
						// model.getPostset(t).remove(pre);
					}
					System.out.println("here?");
					deleteSilentTransition(pre, model);
					model.removePlace(pre);
				}
			}

			for (Place succ : succPlace) {
				if (model.getPreset(succ).size() - getPreSilentTransitions(succ, model).size() > 1) {
					model.getPreset(succ).remove(tt);
					System.out.println("here....3");
				} else {
					Set<Transition> succSet = model.getPostset(succ);
					System.out.println("lala");
					for (Transition t : succSet) {
						if (model.getPreset(t).size() - getPreSilenNum(t, model) > 1) {
							model.getPreset(t).addAll(model.getPreset(tt));
							for (Place p : prePlace) {
								model.addFlow(p, t);
							}
						}else{
							if((model.getPostset(succ).size() - getSuccSilentTransitions(succ, model).size() >= 1)
									|| model.getPostsetTransitions(model.getPostset(t)).size() - getSuccSilenNum(t, model) >= 1){
								System.out.println("haha");
								for(Place p: prePlace){
									model.addFlow(p, t);
								}
							}
						}
						model.getPreset(t).remove(succ);
					}
					deleteSilentTransition(succ, model);
					model.removePlace(succ);
				}
			}
			model.removeTransition(tt);
		}

		System.out.println("model t:" + ":" + tt.getLabel() + ":" + model.getTransitions().size());
		if (model.getTransitions().size() - getSilentTransitions(model).size() == 0 ){
			System.out.println("zero");
			model.clear();
		}
		return model;
	}

	private static ArrayList<Transition> getSilentTransitions(NetSystem model) {
		// TODO Auto-generated method stub
		ArrayList<Transition> silents = new ArrayList<Transition>();
		for (Transition t : model.getTransitions()) {
			if (t.getLabel().contains(Relation.SLIENTTRANSITION))
				silents.add(t);
		}
		return silents;
	}

	public static NetSystem deleteSilentTransition(Set<Place> succPlace, NetSystem model) {
		for (Transition t : model.getPostsetTransitions(succPlace)) {
			if (t.getLabel().contains(Relation.SLIENTTRANSITION))
				model.removeTransition(t);
		}
		return model;
	}

	// delete the silent transitions
	public static NetSystem deleteSilentTransition(Place place, NetSystem model) {
		// pre delete
		ArrayList<Transition> succsilents = getSuccSilentTransitions(place, model);
		ArrayList<Transition> preSilents = getPreSilentTransitions(place, model);
		System.out.println(succsilents.size() + "::" + preSilents.size());
		if (succsilents.size() > 0) {
			for (Transition t : succsilents) {
				System.out.println("succ:" + t.getLabel());
				if (getSinkTransitions(model).contains(t) && model.getPreset(t).size() == 1) { // the

					for (Place succ : model.getPostset(t)) {
						if (t.getLabel().contains(Relation.SLIENTTRANSITION)
								&& model.getPreset(succ).size() - getPreSilentTransitions(succ, model).size() >= 1)
							continue;
						else if (!t.getLabel().contains(Relation.SLIENTTRANSITION)
								&& model.getPreset(succ).size() - getPreSilentTransitions(succ, model).size() > 1)
							continue;
						else {
							boolean flag = false;
							for (Transition tt : model.getPreset(succ)) {
								if (model.getPreset(place).size() - getPreSilentTransitions(place, model).size() == 0 || tt.getLabel().contains(Relation.SLIENTTRANSITION + "start")) {
									flag = true;
									break;
								}
							}
							if (flag) {
								model.removeTransitions(model.getPostsetTransitions(model.getPostset(t)));
								model.getPostset(place).remove(t);
								model.removePlaces(model.getPostset(t));
								System.out.println("succ 111111111111111111111");
							}
						}
					}
					// model.removePlaces(model.getPostset(t));

					model.removePlaces(model.getPreset(t));
					model.removeTransition(t);
					System.out.println("abcccc");
				} else if (model.getPreset(t).size() > 1) {
					// the silent transition could not be deleted
					model.getPreset(t).remove(place);
					if (model.getPresetTransitions(model.getPreset(t))
							.contains(new Transition(Relation.SLIENTTRANSITION + "start")))
						model.removeTransition(t);
					System.out.println("abcc");
				} else {
					// delete the silent transition
					System.out.println("adc");
					if (model.getPresetTransitions(model.getPreset(t)).size() > 0) {
						for (Place succ : model.getPostset(t)) {
							if (model.getPostset(succ).size() - getSuccSilentTransitions(succ, model).size() != 0) {
								for (Transition ti : model.getPresetTransitions(model.getPreset(t))){
									model.addFlow(ti, succ);
								}
							} else {
								if(model.getPostset(succ).size() - getSuccSilentTransitions(succ,model).size() == 0){
									boolean flag = false;
									for(Transition tt: model.getPostset(succ)){
										if(getSinkTransitions(model).contains(tt))
											flag = true;
									}
									if(flag)
										deleteSilentTransition(succ, model);
								}
								model.removePlace(succ);
								
							}
						}
					}
					else if(model.getPresetTransitions(model.getPreset(t)).size() == 0)
						model.removePlaces(model.getPreset(t));
					
					//model.removePlaces(model.getPreset(t));
					model.removeTransition(t);
				}

			}
		}

		if (preSilents.size() > 0) {
			for (Transition t : preSilents) {
				if (model.getPostset(t).size() == 1 && getSourceTransitions(model).contains(t)) { // the
																									// silent
																									// transition
																									// is
																									// source
																									// node
					for (Place pre : model.getPreset(t)) {
						if (t.getLabel().contains(Relation.SLIENTTRANSITION)
								&& model.getPostset(pre).size() - getSuccSilentTransitions(pre, model).size() >= 1)
							continue;

						else if (!t.getLabel().contains(Relation.SLIENTTRANSITION)
								&& model.getPostset(pre).size() - getSuccSilentTransitions(pre, model).size() > 1)
							continue;

						else {
							// inv_ start -> inv_end delete
							boolean flag = false;
							for (Transition tt : model.getPostset(pre)) {
								if (model.getPostset(place).size() - getSuccSilentTransitions(place, model).size() == 0 || tt.getLabel().contains(Relation.SLIENTTRANSITION + "end")) {
									flag = true;
									break;
								}
							}
							if (flag) {
								model.removePlaces(model.getPreset(t));
								model.removeTransitions(model.getPresetTransitions(model.getPreset(t)));
								model.getPreset(place).remove(t);
								System.out.println("pre 11111111111111111111111111111");
							}
						}
					}
					model.removePlaces(model.getPostset(t));
					model.removeTransition(t);
					System.out.println("pre 11111111111111111111111111111111111112");

				} else if (model.getPostset(t).size() > 1) { // the silent
																// transition
																// could not be
																// deleted
					model.getPostset(t).remove(place);
					if (model.getPostsetTransitions(model.getPostset(t))
							.contains(new Transition(Relation.SLIENTTRANSITION + "end"))) {
						model.removeTransition(t);
						System.out.println("silent delete.....");
					}
				} else { // delete the silent transition
					if (model.getPostsetTransitions(model.getPostset(t)).size() > 0) {
						for (Place pre : model.getPreset(t)) {
							if (model.getPreset(pre).size() - getPreSilentTransitions(pre, model).size() != 0) {
								for (Transition ti : model.getPostsetTransitions(model.getPostset(t)))
									model.addFlow(pre, ti);
							} else {
								if(model.getPreset(pre).size() - getPreSilentTransitions(pre,model).size() == 0){
									boolean flag = false;
									for(Transition tt: model.getPreset(pre)){
										if(getSourceTransitions(model).contains(tt))
											flag = true;
									}
									if(flag)
										deleteSilentTransition(pre, model);
								}
								model.removePlace(pre);
							}
						}
					}
					model.removePlaces(model.getPostset(t));
					model.removeTransition(t);
					System.out.println("pre...........................4");
				}
			}
		}

		return model;
	}

	// delete the transition in the model
	public static NetSystem deleteOp(ArrayList dif, NetSystem model) {
		for (Transition tt : (ArrayList<Transition>) dif) {
			// 1.only the one node in the model
			if (getSinkTransitions(model).contains(tt) && getSourceTransitions(model).contains(tt))
				return null;
			PetriNet petri = PetriNetConversion.convert(model);
			FileUtil.Export(petri, "test/test1" + tt.getLabel() + ".pnml");
			deleteOneTransition(tt, model);
		}
		return model;
	}

	// insert the transition into model
	public static NetSystem insertOp(ArrayList<String> dif, NetSystem model) {
		return model;
	}
}