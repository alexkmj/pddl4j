/*
 * Copyright (c) 2016 by Damien Pellier <Damien.Pellier@imag.fr>.
 *
 * This file is part of PDDL4J library.
 *
 * PDDL4J is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * PDDL4J is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with PDDL4J.  If not, see
 * <http://www.gnu.org/licenses/>
 */

package fr.uga.pddl4j.planners.statespace.search.strategy;

import fr.uga.pddl4j.encoding.CodedProblem;
import fr.uga.pddl4j.heuristics.relaxation.Heuristic;
import fr.uga.pddl4j.planners.statespace.StateSpacePlanner;
import fr.uga.pddl4j.util.BitOp;
import fr.uga.pddl4j.util.Plan;
import fr.uga.pddl4j.util.SequentialPlan;

import java.util.Objects;

/**
 * This abstract class defines the main methods for search strategies.
 *
 * @author E. Hermellin
 * @version 1.0 - 11.06.2018
 * @since 3.6
 */
public abstract class AbstractStateSpaceStrategy implements StateSpaceStrategy {

    /**
     * The serial id of the class.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The heuristic of the planner.
     */
    private Heuristic.Type heuristic;

    /**
     * The heuristic weight.
     */
    private double weight;

    /**
     * The timeout for the search in second.
     */
    private int timeout;

    /**
     * The time spend to find a solution.
     */
    private long searchingTime;

    /**
     * The root node of the state space.
     */
    private Node rootNode;

    /**
     * Returns the heuristicType to use to solve the planning problem.
     *
     * @return the heuristicType to use to solve the planning problem.
     */
    @Override
    public final Heuristic.Type getHeuristicType() {
        return this.heuristic;
    }

    /**
     * Sets the heuristicType to use to solved the problem.
     *
     * @param heuristicType the heuristicType to use to solved the problem. The heuristicType cannot be null.
     */
    @Override
    public final void setHeuristicType(final Heuristic.Type heuristicType) {
        Objects.requireNonNull(heuristicType);
        this.heuristic = heuristicType;
    }

    /**
     * Returns the weight set to the heuristic.
     *
     * @return the weight set to the heuristic.
     */
    @Override
    public final double getWeight() {
        return this.weight;
    }

    /**
     * Sets the wight of the heuristic.
     *
     * @param weight the weight of the heuristic. The weight must be positive.
     */
    @Override
    public final void setWeight(final double weight) {
        this.weight = weight;
    }

    /**
     * Sets the time out of the planner.
     *
     * @param timeout the time allocated to the search in second. Timeout mus be positive.
     */
    @Override
    public final void setTimeOut(final int timeout) {
        this.timeout = timeout;
    }

    /**
     * Returns the time out of the planner.
     *
     * @return the time out of the planner, i.e., the time allocated to the search in second.
     */
    @Override
    public int getTimeout() {
        return this.timeout;
    }

    /**
     * Returns the time spend to find a solution.
     *
     * @return the time spend to find a solution.
     */
    @Override
    public long getSearchingTime() {
        return searchingTime;
    }

    /**
     * Sets the time out of the planner.
     *
     * @param searchingTime the time allocated to the search in second. Timeout mus be positive.
     */
    @Override
    public void setSearchingTime(final long searchingTime) {
        this.searchingTime = searchingTime;
    }

    /**
     * Returns the root node of the state space.
     *
     * @return the root node of the state space.
     */
    @Override
    public Node getRootNode() {
        return this.rootNode;
    }

    /**
     * Sets the root node of the state space.
     *
     * @param rootNode the root node of the state space.
     */
    @Override
    public void setRootNode(final Node rootNode) {
        this.rootNode = rootNode;
    }

    /**
     * Create a new search strategy.
     */
    public AbstractStateSpaceStrategy() {
        super();
        this.heuristic = StateSpacePlanner.DEFAULT_HEURISTIC;
        this.weight = StateSpacePlanner.DEFAULT_WEIGHT;
        this.timeout = StateSpacePlanner.DEFAULT_TIMEOUT;
        this.searchingTime = 0;
    }

    /**
     * Create a new search strategy.
     *
     * @param heuristic the heuristicType to use to solve the planning problem.
     * @param timeout   the time out of the planner.
     * @param weight    the weight set to the heuristic.
     */
    public AbstractStateSpaceStrategy(int timeout, Heuristic.Type heuristic, double weight) {
        super();
        this.timeout = timeout;
        this.heuristic = heuristic;
        this.weight = weight;
        this.searchingTime = 0;
    }

    /**
     * Search a solution node to a specified domain and problem.
     *
     * @param codedProblem the problem to be solved. The problem cannot be null.
     * @return the solution node or null.
     */
    @Override
    public Node searchSolutionNode(final CodedProblem codedProblem) {
        Objects.requireNonNull(codedProblem);
        return search(codedProblem);
    }

    /**
     * Search a solution plan to a specified domain and problem.
     *
     * @param codedProblem the problem to be solved. The problem cannot be null.
     * @return the solution plan or null.
     */
    @Override
    public Plan searchPlan(final CodedProblem codedProblem) {
        Objects.requireNonNull(codedProblem);
        final Node solutionNode = search(codedProblem);
        if (solutionNode != null) {
            return extractPlan(solutionNode, codedProblem);
        } else {
            return null;
        }
    }

    /**
     * Extract a plan from a solution node for the specified planning problem.
     *
     * @param node    the solution node.
     * @param problem the problem to be solved.
     * @return the solution plan or null is no solution was found.
     */
    @Override
    public SequentialPlan extractPlan(final Node node, final CodedProblem problem) {
        if (node != null) {
            Node n = node;
            final SequentialPlan plan = new SequentialPlan();
            while (n.getParent() != null) {
                final BitOp op = problem.getOperators().get(n.getOperator());
                plan.add(0, op);
                n = n.getParent();
            }
            return plan;
        } else {
            return null;
        }
    }
}
