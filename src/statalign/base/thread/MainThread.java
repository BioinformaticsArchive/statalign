package statalign.base.thread;

import java.io.File;

import statalign.base.MainManager;
import statalign.base.Mcmc;
import statalign.base.Tree;
import statalign.io.RawSequences;
import statalign.ui.ErrorMessage;

/**
 * The main (suspendable) thread for background MCMC calculation.
 *
 * @author novak
 */
public class MainThread extends StoppableThread {
	
	/**
	 * Reference to the (singleton) MainManager object encapsulating all settings and data
	 * that an MCMC run depends on.
	 */
	public MainManager owner;
	
	/**
	 * Constructs a new MainThread that can be used to fire a background MCMC calculation.
	 * 
	 * @param owner Reference to the MainManager object.
	 */
	public MainThread(MainManager owner) {
		this.owner = owner;
	}
	/**
	 * Start background MCMC calculation.
	 */
	@Override
	public synchronized void run() {
		try {
			RawSequences seqs = owner.inputData.seqs;
			
			if(owner.frame != null) {
				owner.frame.statusText.setText(" Generating initial tree and alignment...");
			}

			System.out.println("\nPreparing initial tree and alignment...\n");

			// remove gaps and whitespace
			owner.inputData.title = new File(owner.fullPath).getName();
			String[] nongapped = new String[seqs.sequences.size()];
			StringBuilder builder = new StringBuilder();
			int i, j;
			char ch;
			for(i = 0; i < nongapped.length; i++) {
				builder.setLength(0);
				String seq = seqs.sequences.get(i);
				for(j = 0; j < seq.length(); j++) {
					ch = seq.charAt(j);
					if(Character.isWhitespace(ch) || ch == '-')
						continue;
					builder.append(ch);
				}
				nongapped[i] = builder.toString();
			}
			
			Tree tree = new Tree(nongapped, seqs.seqNames.toArray(new String[seqs.seqNames.size()]), 	
					owner.inputData.model,
					owner.inputData.model.attachedScoringScheme,
					new File(owner.fullPath).getName());
			Mcmc mcmc = new Mcmc(tree, owner.inputData.pars, owner.postProcMan);
			mcmc.doMCMC();
			owner.finished(false);
			System.out.println("Ready.");
		} catch(StoppedException e) {
			owner.finished(true);
			System.out.println("Interrupted.");
		} catch(Exception e) {
			e.printStackTrace();
			if(owner.frame != null) {
				ErrorMessage.showPane(owner.frame, e, true);
			}
			owner.finished(true);
		}
	}
}
