package statalign.model.ext;

import statalign.model.ext.ModelExtension;
import statalign.model.ext.ParameterInterface;
import statalign.model.ext.PriorDistribution;

public abstract class McmcMove {

	protected ModelExtension owner;
	protected PriorDistribution<? extends Object> prior;
	
	protected ParameterInterface param;
	public ParameterInterface getParam() {
		return param;
	}
	
	public int proposalCount = 0;
	public int acceptanceCount = 0;
	public boolean lastMoveAccepted = false;
	
	public String name;
	public double proposalWidthControlVariable;
	public boolean autoTune;
	
	public double acceptanceRate() {
		return (double) acceptanceCount / (double) proposalCount;
	}
	
	public abstract void copyState(Object externalState);
	public abstract double proposal(Object externalState); 
	// Modifies variables and returns logProposalRatio
	
	public abstract double logPriorDensity(Object externalState);
	public abstract void updateLikelihood(Object externalState); 
	public abstract void restoreState(Object externalState);
	
	public void move(Object externalState) {
		
		proposalCount++;
		copyState(externalState);
		double logProposalRatio = -logPriorDensity(externalState);
		logProposalRatio = proposal(externalState); 
		logProposalRatio += logPriorDensity(externalState);
		updateLikelihood(externalState);
		if(owner.isParamChangeAccepted(logProposalRatio)) {
			acceptanceCount++;
			lastMoveAccepted = true;
		}
		else {
			lastMoveAccepted = false;
			restoreState(externalState);
		}
	}
	
	 
	
}