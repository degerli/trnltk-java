package org.trnltk.morphology.morphotactics;

import com.google.common.base.Predicate;
import org.junit.Test;
import org.trnltk.morphology.contextless.parser.formbased.SampleSuffixGraph;
import org.trnltk.morphology.model.suffixbased.SuffixGroup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SuffixGraphDrawingTest {

    @Test
    public void shouldDumpBasicSuffixGraphInDotFormat() throws Exception {
        final BasicSuffixGraph graph = new BasicSuffixGraph();
        graph.initialize();


        this.dumpSuffixGraphInDotFormat(graph, null, null);
    }

    @Test
    public void shouldDumpSampleGraphInDotFormat() throws Exception {
        final BaseSuffixGraph graph = new SampleSuffixGraph();
        graph.initialize();


        this.dumpSuffixGraphInDotFormat(graph, null, null);
    }

    @Test
    public void shouldDumpNumeralSuffixGraphInDotFormat() throws Exception {
        final NumeralSuffixGraph graph = new NumeralSuffixGraph(new BasicSuffixGraph());
        graph.initialize();


        this.dumpSuffixGraphInDotFormat(graph, null, null);
    }

    @Test
    public void shouldDumpBigSuffixGraphInDotFormat() throws Exception {
        final CopulaSuffixGraph graph = new CopulaSuffixGraph(new ProperNounSuffixGraph(new NumeralSuffixGraph(new BasicSuffixGraph())));
        graph.initialize();


        this.dumpSuffixGraphInDotFormat(graph, null, null);
    }

    @Test
    public void shouldDumpNumeralSuffixGraphInDotFormatForNumeralsAndAdj() throws Exception {
        final NumeralSuffixGraph graph = new NumeralSuffixGraph(new BasicSuffixGraph());
        graph.initialize();


        Predicate<SuffixGraphState> sourceNodePredicate = new Predicate<SuffixGraphState>() {
            @Override
            public boolean apply(SuffixGraphState input) {
                return input.getName().startsWith("NUMERAL") || input.getName().startsWith("ADJ");
            }
        };
        Predicate<SuffixGraphState> targetNodePredicate = new Predicate<SuffixGraphState>() {
            @Override
            public boolean apply(SuffixGraphState input) {
                return input.getName().startsWith("NUMERAL") || input.getName().startsWith("ADJ");
            }
        };
        this.dumpSuffixGraphInDotFormat(graph, sourceNodePredicate, targetNodePredicate);
    }

    @Test
    public void shouldDumpNumeralSuffixGraphInDotFormatForVerbs() throws Exception {
        final NumeralSuffixGraph graph = new NumeralSuffixGraph(new BasicSuffixGraph());
        graph.initialize();


        Predicate<SuffixGraphState> sourceNodePredicate = new Predicate<SuffixGraphState>() {
            @Override
            public boolean apply(SuffixGraphState input) {
                return input.getName().startsWith("VERB");
            }
        };
        Predicate<SuffixGraphState> targetNodePredicate = new Predicate<SuffixGraphState>() {
            @Override
            public boolean apply(SuffixGraphState input) {
                return input.getName().startsWith("VERB");
            }
        };
        this.dumpSuffixGraphInDotFormat(graph, sourceNodePredicate, targetNodePredicate);
    }

    @Test
    public void shouldDumpBigSuffixGraphInDotFormatForVerbAndAdjRelatedNodes() throws Exception {
        final CopulaSuffixGraph graph = new CopulaSuffixGraph(new ProperNounSuffixGraph(new NumeralSuffixGraph(new BasicSuffixGraph())));
        graph.initialize();


        Predicate<SuffixGraphState> sourceNodePredicate = new Predicate<SuffixGraphState>() {
            @Override
            public boolean apply(SuffixGraphState input) {
                return input.getName().startsWith("VERB") || input.getName().startsWith("ADJ");
            }
        };
        Predicate<SuffixGraphState> targetNodePredicate = new Predicate<SuffixGraphState>() {
            @Override
            public boolean apply(SuffixGraphState input) {
                return input.getName().startsWith("VERB") || input.getName().startsWith("ADJ");
            }
        };
        this.dumpSuffixGraphInDotFormat(graph, sourceNodePredicate, targetNodePredicate);
    }

    @Test
    public void shouldDumpBigSuffixGraphInDotFormatForVerbNounAndAdvRelatedNodes() throws Exception {
        final CopulaSuffixGraph graph = new CopulaSuffixGraph(new ProperNounSuffixGraph(new NumeralSuffixGraph(new BasicSuffixGraph())));
        graph.initialize();


        Predicate<SuffixGraphState> sourceNodePredicate = new Predicate<SuffixGraphState>() {
            @Override
            public boolean apply(SuffixGraphState input) {
                return input.getName().startsWith("VERB") || input.getName().startsWith("ADV") || input.getName().startsWith("NOUN");
            }
        };
        Predicate<SuffixGraphState> targetNodePredicate = new Predicate<SuffixGraphState>() {
            @Override
            public boolean apply(SuffixGraphState input) {
                return input.getName().startsWith("VERB") || input.getName().startsWith("ADV") || input.getName().startsWith("NOUN");
            }
        };
        this.dumpSuffixGraphInDotFormat(graph, sourceNodePredicate, targetNodePredicate);
    }

    @Test
    public void shouldDumpBigSuffixGraphInDotFormatForNounRelatedNodes() throws Exception {
        final CopulaSuffixGraph graph = new CopulaSuffixGraph(new ProperNounSuffixGraph(new NumeralSuffixGraph(new BasicSuffixGraph())));
        graph.initialize();


        Predicate<SuffixGraphState> sourceNodePredicate = new Predicate<SuffixGraphState>() {
            @Override
            public boolean apply(SuffixGraphState input) {
                return input.getName().startsWith("NOUN");
            }
        };
        Predicate<SuffixGraphState> targetNodePredicate = new Predicate<SuffixGraphState>() {
            @Override
            public boolean apply(SuffixGraphState input) {
                return input.getName().startsWith("NOUN");
            }
        };
        this.dumpSuffixGraphInDotFormat(graph, sourceNodePredicate, targetNodePredicate);
    }


    public void dumpSuffixGraphInDotFormat(BaseSuffixGraph theGraph, Predicate<SuffixGraphState> sourceNodePredicate, Predicate<SuffixGraphState> targetNodePredicate) {
        System.out.println("digraph suffixGraph {");

        final Set<String> nodeNames = new HashSet<String>();
        int edgeCount = 0;
        final HashMap<String, String> suffixGroupColorMap = new HashMap<String, String>();

        BaseSuffixGraph graph = theGraph;
        while (graph != null) {
            for (SuffixGraphState state : graph.stateMap.values()) {
                if (sourceNodePredicate != null && !sourceNodePredicate.apply(state))
                    continue;

                final String sourceStateName = state.getName();
                final boolean sourceStateAdded = nodeNames.add(sourceStateName);
                if (sourceStateAdded)
                    System.out.println(String.format("\t%s [shape=\"%s\"]", sourceStateName, getNodeShape(state)));

                for (SuffixEdge suffixEdge : state.getOutEdges()) {
                    final SuffixGraphState targetState = suffixEdge.getTargetState();
                    if (targetNodePredicate != null && !targetNodePredicate.apply(targetState))
                        continue;

                    final String targetStateName = targetState.getName();
                    final boolean targetStateAdded = nodeNames.add(targetStateName);

                    if (targetStateAdded)
                        System.out.println(String.format("\t%s [shape=\"%s\"]", targetStateName, getNodeShape(targetState)));


                    String style = "solid";
                    String color = getEdgeColor(suffixEdge, suffixGroupColorMap);
                    String label = suffixEdge.getSuffix().getName();
                    String line = "\t%s -> %s [style=\"%s\" color=\"%s\" label=\"%s\"]";
                    System.out.println(String.format(line, sourceStateName, targetStateName, style, color, label));

                    edgeCount++;
                }
            }

            if (graph.decorated instanceof BaseSuffixGraph)
                graph = (BaseSuffixGraph) graph.decorated;
            else
                graph = null;
        }

        System.out.println("// Number of nodes : " + nodeNames.size());
        System.out.println("// Number of edges: " + edgeCount);

        System.out.println("}");

    }

    private String getEdgeColor(SuffixEdge suffixEdge, HashMap<String, String> suffixGroupColorMap) {
        //see http://www.graphviz.org/doc/info/attrs.html#k:colorList
        final SuffixGroup suffixGroup = suffixEdge.getSuffix().getSuffixGroup();
        if (suffixGroup == null)
            return "black";

        if (!suffixGroupColorMap.containsKey(suffixGroup.getName()))
            suffixGroupColorMap.put(suffixGroup.getName(), getRandomColor());

        return suffixGroupColorMap.get(suffixGroup.getName());
    }

    private String getNodeShape(SuffixGraphState state) {
        //see http://www.graphviz.org/doc/info/shapes.html
        if (state.getName().endsWith("_ROOT") || state.getName().endsWith("_ROOT_TERMINAL"))
            return "circle";
        else if (state.getType().equals(SuffixGraphStateType.DERIVATIONAL))
            return "house";
        else if (state.getType().equals(SuffixGraphStateType.TERMINAL))
            return "doubleoctagon";
        else
            return "ellipse";
    }

    private String getRandomColor() {
        int r = Double.valueOf(Math.random() * 0xd0).intValue() + 0x10;
        int g = Double.valueOf(Math.random() * 0xd0).intValue() + 0x10;
        int b = Double.valueOf(Math.random() * 0xd0).intValue() + 0x10;

        return "#" + Integer.toHexString(r) + Integer.toHexString(g) + Integer.toHexString(b);

    }
}