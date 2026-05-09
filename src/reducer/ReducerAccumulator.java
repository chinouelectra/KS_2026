package reducer;

import common.map_reduce.Reducer;

import java.util.LinkedHashMap;
import java.util.Map;

public class ReducerAccumulator implements Reducer<String, Double, Map<String, Double>> {

    @Override
    public Map<String, Double> reduce(Map<String, Double> partialTotals) {
        Map<String, Double> reducedTotals = new LinkedHashMap<>();
        double grandTotal = 0.0;

        for (Map.Entry<String, Double> entry : partialTotals.entrySet()) {
            reducedTotals.merge(entry.getKey(), entry.getValue(), Double::sum);
            grandTotal += entry.getValue();
        }

        reducedTotals.put("Total", grandTotal);
        return reducedTotals;
    }

    public void accumulate(Map<String, Double> mergedTotals, Map<String, Double> partialTotals) {
        if (partialTotals == null) {
            return;
        }

        for (Map.Entry<String, Double> entry : partialTotals.entrySet()) {
            mergedTotals.merge(entry.getKey(), entry.getValue(), Double::sum);
        }
    }

    public Map<String, Double> finalizeProviderTotals(Map<String, Double> mergedTotals) {
        return reduce(mergedTotals);
    }

    public Map<String, Double> finalizePlayerTotals(Map<String, Double> mergedTotals) {
        Map<String, Double> reducedTotals = new LinkedHashMap<>();
        reducedTotals.put("Total Profit/Loss", mergedTotals.getOrDefault("Total Profit/Loss", 0.0));
        return reducedTotals;
    }
}
