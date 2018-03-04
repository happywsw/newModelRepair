/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 *
 * LICENSE:
 *
 * This code is free software; you can redistribute it and/or
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 * EXEMPTION:
 *
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 *
 */
package org.processmining.analysis.benchmark.metric;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.ui.Progress;

/**
 * This interface must be implemented by process metrics to be used
 * for process mining plugin benchmarking. Benchmarking metrics are
 * supposed to be Petri net based and return results with double
 * precision, within a value range of <code>[0, 1]</code>.
 *
 * @author Christian W. Guenther (christian@deckfour.org)
 *
 */
public interface BenchmarkMetric {

	public static final double INVALID_MEASURE_VALUE = -1; //indicates that the metric could not be calculated for the
	                                                    //the provided input net(s) and log.

	/**
	 * Measures the quality of a mining algorithm's result.
	 * Metric needs to be Petri net based.
	 * @param model The resulting Petri net generated by a mining algorithm.
	 * @param referenceLog A log which may be used to measure conformance to
	 * 	the mined model.
	 * 	<p><b>Note:</b> This is an optional parameter, i.e. it may
	 *  be <code>null</code> for some applications.
	 * @param referenceModel A Petri net which may be used to measure conformance to
	 * 	the mined model.
	 * 	<p><b>Note:</b> This is an optional parameter, i.e. it may
	 *  be <code>null</code> for some applications.
	 * @progress This parameter can, and should, be used to signal progress of the metric
	 *  measurement process back to the benchmark framework. You should set the
	 *  progress to <code>0</code> when starting your measurement, at the end the progress
	 *  bar should be at its maximal position.
	 * @return The degree of quality as measured by this metric implementation for the
	 * 	given set of parameters. Returned values must be within <code>[0, 1]</code> in
	 * 	double precision. If the metric cannot be calculated, the value <code>BenchmarkMetric.INVALID_MEASURE_VALUE</code>
	 *  should be returned.
	 */
	public double measure(PetriNet model, LogReader referenceLog, PetriNet referenceModel, Progress progress);

	/**
	 * @return A human-readable name of this metric (less than 30 characters, preferably)
	 */
	public String name();
	
	/**
	 * @return A short help text about this metric (what does it measure?). 
	 */
	public String description();

	/**
	 * Probes whether this metric - in addition to the given model - needs a reference model
	 * to compute its measurements.
	 */
	public boolean needsReferenceModel();

	/**
	 * Probes whether this metric needs a reference log to compute its
	 * measurements.
	 */
	public boolean needsReferenceLog();
}
