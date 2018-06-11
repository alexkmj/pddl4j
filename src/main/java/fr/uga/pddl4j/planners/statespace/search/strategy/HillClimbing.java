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
import fr.uga.pddl4j.heuristics.relaxation.HeuristicToolKit;
import fr.uga.pddl4j.util.BitOp;
import fr.uga.pddl4j.util.BitState;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;

/**
 * This class implements Hill Climnbing search strategy.
 *
 * @author D. Pellier
 * @version 1.0 - 01.06.2018
 */
public final class HillClimbing extends AbstractStateSpaceStrategy {

    /**
     * The serial id of the class.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new Hill Climbing search strategy.
     *
     * @param timeout   the time out of the planner.
     * @param heuristic the heuristicType to use to solve the planning problem.
     * @param weight    the weight set to the heuristic.
     */
    public HillClimbing(int timeout, Heuristic.Type heuristic, double weight) {
        super();
        this.setTimeOut(timeout);
        this.setHeuristicType(heuristic);
        this.setWeight(weight);
    }

    /**
     * The hill climbing algorithm. Solves the planning problem and returns the solution's node.
     *
     * @return the solution node.
     */
    public Node search(final CodedProblem codedProblem) {
        Objects.requireNonNull(codedProblem);
        final LinkedList<Node> openList = new LinkedList<>();
        final Heuristic heuristic = HeuristicToolKit.createHeuristic(getHeuristicType(), codedProblem);
        double bestHeuristic = Integer.MAX_VALUE;

        BitState init = new BitState(codedProblem.getInit());
        Node root = new Node(init, null, 0, 0, heuristic.estimate(init, codedProblem.getGoal()));
        this.setRootNode(root);
        openList.add(root);

        Node solution = null;
        boolean deadEndFree = true;

        final int timeout = getTimeout();
        final long begin = System.currentTimeMillis();
        long searchingTime = 0;
        while (!openList.isEmpty() && solution == null
            && deadEndFree && searchingTime < timeout) {

            final Node currentState = openList.pop();
            final LinkedList<Node> successors = HillClimbing.getSuccessors(currentState, codedProblem, heuristic);
            deadEndFree = !successors.isEmpty();

            final Node successor = HillClimbing.popBestNode(successors, bestHeuristic);
            if (successor.satisfy(codedProblem.getGoal())) {
                solution = successor;
            } else {
                successors.clear();
                openList.clear();
                openList.addLast(successor);
                if (successor.getHeuristic() <= bestHeuristic) {
                    bestHeuristic = successor.getHeuristic();
                }
            }

            long end = System.currentTimeMillis();
            searchingTime = end - begin;
        }

        this.setSearchingTime(searchingTime);

        return solution;
    }

    /**
     * Get the successors from a node.
     *
     * @param parent    the parent node.
     * @param problem   the coded problem to solve.
     * @param heuristic the heuristic used.
     * @return the list of successors from the parent node.
     */
    private static LinkedList<Node> getSuccessors(final Node parent, final CodedProblem problem,
                                                  final Heuristic heuristic) {
        final LinkedList<Node> successors = new LinkedList<>();

        int index = 0;
        for (BitOp op : problem.getOperators()) {
            if (op.isApplicable(parent)) {
                final BitState nextState = new BitState(parent);
                nextState.or(op.getCondEffects().get(0).getEffects().getPositive());
                nextState.andNot(op.getCondEffects().get(0).getEffects().getNegative());

                // Apply the effect of the applicable operator
                final Node successor = new Node(nextState);
                successor.setCost(parent.getCost() + op.getCost());
                successor.setHeuristic(heuristic.estimate(nextState, problem.getGoal()));
                successor.setParent(parent);
                successor.setOperator(index);
                successor.setDepth(parent.getDepth() + 1);
                successors.add(successor);
            }
            index++;
        }

        return successors;
    }

    /**
     * Return the best node from a list according to the heuristic value.
     *
     * @param nodes the list containing nodes.
     * @return the best node from the nodes' list.
     */
    private static Node popBestNode(final Collection<Node> nodes, final double bestHeuristic) {
        Node node = null;
        if (!nodes.isEmpty()) {
            final Iterator<Node> i = nodes.iterator();
            node = i.next();
            while (i.hasNext()) {
                final Node next = i.next();
                if (next.getHeuristic() < bestHeuristic) {
                    node = next;
                }
            }
            nodes.remove(node);
        }
        return node;
    }
}
