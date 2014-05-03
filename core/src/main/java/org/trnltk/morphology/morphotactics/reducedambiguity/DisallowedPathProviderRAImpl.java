package org.trnltk.morphology.morphotactics.reducedambiguity;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.trnltk.model.suffix.Suffix;
import org.trnltk.model.suffix.SuffixTransition;
import org.trnltk.morphology.contextless.parser.SuffixFormGraphSuffixEdge;
import org.trnltk.morphology.morphotactics.DisallowedPathProvider;
import org.trnltk.morphology.morphotactics.SuffixEdge;
import org.trnltk.morphology.morphotactics.SuffixGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Ali Ok (ali.ok@apache.org)
 */
public class DisallowedPathProviderRAImpl implements DisallowedPathProvider {
    private final SuffixGraph suffixGraph;

    private final Multimap<Suffix, DisallowedPathRule> ruleMap = HashMultimap.create();

    public DisallowedPathProviderRAImpl(SuffixGraph suffixGraph) {
        this.suffixGraph = suffixGraph;
    }

    @Override
    public void initialize() {
        this.createRules();
    }

    private void createRules() {
        // in docs, $ means terminal

        // NOT ALLOWED: kirmizi --> kirmizi+Adj+Noun+Zero+..Nom$
        // NOT ALLOWED: kirmizili --> kirmizi+Adj+li+Adj+Noun+Zero+..Nom$
        // ALLOWED: kirmizinin --> kirmizi+Adj+Noun+Zero+..Gen
        // ALLOWED: kirmizilasmak --> kirmizi+Adj+Verb+Become+..+Noun+Inf+..Nom$
        addRule(DisallowedPathRuleType.CHECK_LAST_DERIVATION_GROUP, "Adj_to_Noun_Zero_Transition", "A3Sg_Noun", "Pnon_Noun", "Nom_Noun", "Noun_Free_Transition_2");

        // NOT ALLOWED: benim yaparim --> yap+Verb+Aor+Adj+Zero+Noun+Zero+..P1sg
        addRule(DisallowedPathRuleType.CHECK_COMPLETE_PATH, "Aorist_to_Adj", "Adj_to_Noun_Zero_Transition", "P1Sg_Noun");

        // NOT ALLOWED: senin yaparin --> yap+Verb+Aor+Adj+Zero+Noun+Zero+..P2sg
        addRule(DisallowedPathRuleType.CHECK_COMPLETE_PATH, "Aorist_to_Adj", "Adj_to_Noun_Zero_Transition", "P2Sg_Noun");

        // NOT ALLOWED: benim yapmisim --> yap+Verb+Narr+Adj+Zero+Noun+Zero+..P1sg
        addRule(DisallowedPathRuleType.CHECK_COMPLETE_PATH, "Narr_to_Adj", "Adj_to_Noun_Zero_Transition", "P1Sg_Noun");

        // NOT ALLOWED: senin yapmisin --> yap+Verb+Narr+Adj+Zero+Noun+Zero+..P2sg
        addRule(DisallowedPathRuleType.CHECK_COMPLETE_PATH, "Narr_to_Adj", "Adj_to_Noun_Zero_Transition", "P2Sg_Noun");
    }

    /**
     * By default add with type {@link org.trnltk.morphology.morphotactics.reducedambiguity.DisallowedPathProviderRAImpl.DisallowedPathRuleType#CHECK_LAST_DERIVATION_GROUP}
     */
    private void addRule(DisallowedPathRuleType disallowedPathRuleType, String... suffixIds) {
        final List<Suffix> suffixes = new ArrayList<>(suffixIds.length);
        for (int i = 0; i < suffixIds.length; i++) {
            final String suffixId = suffixIds[i];
            final Suffix suffix = this.suffixGraph.getSuffix(suffixId);
            if (suffix == null) {
                throw new RuntimeException("Suffix with id '" + suffixId + "' cannot be found!");
            } else {
                suffixes.add(suffix);
            }
        }

        this.ruleMap.put(suffixes.get(suffixIds.length - 1), new DisallowedPathRule(suffixes, disallowedPathRuleType));
    }

    @Override
    public boolean isPathDisallowed(SuffixFormGraphSuffixEdge suffixFormGraphSuffixEdge, List<SuffixTransition> suffixTransitionsOfMorphemeContainer) {
        final Collection<DisallowedPathRule> relevantRules = getRulesForPathsEndingWith(suffixFormGraphSuffixEdge.getSuffixFormApplication().getSuffixForm().getSuffix());
        for (DisallowedPathRule rule : relevantRules) {
            if (rule.matches(suffixTransitionsOfMorphemeContainer)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isPathDisallowed(SuffixEdge suffixEdge, List<SuffixTransition> suffixTransitionsOfMorphemeContainer) {
        final Collection<DisallowedPathRule> relevantRules = getRulesForPathsEndingWith(suffixEdge.getSuffix());
        for (DisallowedPathRule rule : relevantRules) {
            if (rule.matches(suffixTransitionsOfMorphemeContainer)) {
                return true;
            }
        }

        return false;
    }

    private Collection<DisallowedPathRule> getRulesForPathsEndingWith(Suffix suffix) {
        return ruleMap.get(suffix);
    }

    private static class DisallowedPathRule {

        // does not include the path end!
        // that means, for rule "Adj_Noun_Free_Transition, Noun_Terminal", Noun_Terminal is not included in the list below
        private List<Suffix> suffixes;

        private DisallowedPathRuleType disallowedPathRuleType;

        private DisallowedPathRule(List<Suffix> suffixes, DisallowedPathRuleType disallowedPathRuleType) {
            this.suffixes = suffixes;
            this.disallowedPathRuleType = disallowedPathRuleType;
        }

        public boolean matches(List<SuffixTransition> suffixTransitionsOfMorphemeContainer) {
            // current rule start (path start node) is not in the suffixTransitionsOfMorphemeContainer

            // search the path A,B,C in V,A,X,B,Y,C,Z
            // rules are things like:
            // 'don't allow a path passing node A, B and C in order. also not allow when there are intermediate nodes. Path A X B C is also not allowed.'

            // path to check is dependent on the disallowedPathRuleType
            // if it is since last derivation group, only the transitions since last derivation (including the derivation itself) is checked.

            // for path V,A,X,B,Y,C and rule A,B,C this means:
            // last node, node C, is already the current node. We're not gonna do anything about it. path is actually passed without C to this method. So, the path is acually V,A,X,B,Y
            // * check if Node   | is the last node in
            //   --------------    -------------------
            //    B                V,A,X,B,Y
            //    B                V,A,X,B
            //    A                V,A,X
            //    A                V,A
            //// done - found!

            // another case for V,B,X,A,X(,C) and A,B(,C):
            // * check if Node   | is the last node in
            //   --------------    -------------------
            //    B                V,B,X,A,X
            //    B                V,B,X,A
            //    B                V,B,X
            //    B                V,B
            //    A                V
            //// done - not found !

            // from these examples, it is shown clearly that this is a straight forward greedy algorithm
            // --> search the first element, then search second in the remaining, then third in the remaining, then ....

            // Runtime analysis:
            // M = length of suffixTransitionsOfMorphemeContainer
            // N = length of the path
            // worst case => we have to go through all suffix transitions in the morpheme container
            // -> O(M)

            int indexOfSuffix = suffixTransitionsOfMorphemeContainer.size() - 1;
            for (int i = suffixes.size() - 2; i >= 0; i--) {            // ignore last element. we know it is not there as current rule is matched by that.
                final Suffix ruleSuffix = suffixes.get(i);
                indexOfSuffix = searchSuffix(ruleSuffix, suffixTransitionsOfMorphemeContainer, indexOfSuffix);
                if (indexOfSuffix < 0) // if cannot found
                    return false;
            }

            return true;
        }

        private int searchSuffix(Suffix ruleSuffix, List<SuffixTransition> suffixTransitionsOfMorphemeContainer, int startIndex) {
            for (int i = startIndex; i >= 0; i--) {
                SuffixTransition suffixTransition = suffixTransitionsOfMorphemeContainer.get(i);
                if (suffixTransition.getSuffixFormApplication().getSuffixForm().getSuffix().equals(ruleSuffix))
                    return i;

                // if we only need to check until the last derivation -including the derivation itself- and couldn't find anything yet
                // then return -1 to tell caller that we're done.
                if (this.disallowedPathRuleType == DisallowedPathRuleType.CHECK_LAST_DERIVATION_GROUP && suffixTransition.isDerivational())
                    return -1;
            }

            return -1;
        }
    }

    private static enum DisallowedPathRuleType {
        /**
         * Rule only checks the last derivation group including the derivation itself.
         */
        CHECK_LAST_DERIVATION_GROUP,

        /**
         * Rule checks all of suffix transitions
         */
        CHECK_COMPLETE_PATH
    }
}
