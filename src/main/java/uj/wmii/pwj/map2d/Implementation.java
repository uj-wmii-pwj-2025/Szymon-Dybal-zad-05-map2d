package uj.wmii.pwj.map2d;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Implementation<R, C, V> implements Map2D<R, C, V>{
    private Map<R, Map<C, V>> map2D;
    public Implementation(){
        map2D = new HashMap<>();
    }
    @Override
    public V put(R rowKey, C columnKey, V value) {
        if(rowKey == null || columnKey == null){
            throw new NullPointerException("Klucze są nullami");
        }
        Map<C, V> row = map2D.computeIfAbsent(rowKey, k -> new HashMap<>());
        return row.put(columnKey, value);
    }

    @Override
    public V get(R rowKey, C columnKey) {
        if(!map2D.containsKey(rowKey)){
            return null;
        }
        return map2D.get(rowKey).get(columnKey);
    }

    @Override
    public V getOrDefault(R rowKey, C columnKey, V defaultValue) {
        V value = get(rowKey, columnKey);
        if (value == null) {
            return defaultValue;
        }else{
            return value;
        }
    }

    @Override
    public V remove(R rowKey, C columnKey) {
        if(!map2D.containsKey(rowKey)){
            return null;
        }
        Map<C, V> row = map2D.get(rowKey);
        V removed = row.remove(columnKey);
        if(row.isEmpty()){
            map2D.remove(rowKey);
        }
        return removed;
    }

    @Override
    public boolean isEmpty() {
        return map2D.isEmpty();
    }

    @Override
    public boolean nonEmpty() {
        return !map2D.isEmpty();
    }

    @Override
    public int size() {
        int size = 0;
        for(Map<C, V> row : map2D.values()){
            size += row.size();
        }
        return size;
    }

    @Override
    public void clear() {
        map2D.clear();
    }

    @Override
    public Map<C, V> rowView(R rowKey) {
        if(!map2D.containsKey(rowKey)){
            return Map.of();
        }
        return Map.copyOf(map2D.get(rowKey));
    }

    @Override
    public Map<R, V> columnView(C columnKey) {
        Map<R, V> view = new HashMap<>();
        for (var entry : map2D.entrySet()) {
            R rowKey = entry.getKey();
            Map<C, V> row = entry.getValue();
            if (row.containsKey(columnKey)) {
                view.put(rowKey, row.get(columnKey));
            }
        }

        if (view.isEmpty()) {
            return Map.of();
        }

        return Map.copyOf(view);
    }

    @Override
    public boolean containsValue(V value) {
        for(Map<C, V> row : map2D.values()){
            if(row.containsValue(value)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsKey(R rowKey, C columnKey) {
        return map2D.containsKey(rowKey) && map2D.get(rowKey).containsKey(columnKey);
    }

    @Override
    public boolean containsRow(R rowKey) {
        return map2D.containsKey(rowKey);
    }

    @Override
    public boolean containsColumn(C columnKey) {
        for(Map<C, V> row : map2D.values()){
            if(row.containsKey(columnKey)){
                return true;
            }
        }
        return false;
    }

    @Override
    public Map<R, Map<C, V>> rowMapView() {
        Map<R, Map<C, V>> copy = new HashMap<>();
        for (var entry : map2D.entrySet()) {
            R rowKey = entry.getKey();
            Map<C, V> innerCopy = new HashMap<>(entry.getValue());
            copy.put(rowKey, Collections.unmodifiableMap(innerCopy));
        }
        return Collections.unmodifiableMap(copy);
    }

    @Override
    public Map<C, Map<R, V>> columnMapView() {
        Map<C, Map<R, V>> temp = new HashMap<>();
        for (var row : map2D.entrySet()) {
            R rowKey = row.getKey();
            Map<C, V> rowInternalMap = row.getValue();

            for (var colEntry : rowInternalMap.entrySet()) {
                temp.computeIfAbsent(colEntry.getKey(), k -> new HashMap<>()).put(rowKey, colEntry.getValue());
            }
        }
        Map<C, Map<R, V>> result = new HashMap<>();
        for (var entry : temp.entrySet()) {
            result.put(entry.getKey(), Collections.unmodifiableMap(entry.getValue()));
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public Map2D<R, C, V> fillMapFromRow(Map<? super C, ? super V> target, R rowKey) {
        if (map2D.containsKey(rowKey)) {
            Map<C, V> row = map2D.get(rowKey);
            target.putAll(row);
        }
        return this;
    }

    @Override
    public Map2D<R, C, V> fillMapFromColumn(Map<? super R, ? super V> target, C columnKey) {
        for (var entry : map2D.entrySet()) {
            Map<C, V> row = entry.getValue();
            if (row.containsKey(columnKey)) {
                V value = row.get(columnKey);
                target.put(entry.getKey(), value);
            }
        }
        return this;
    }

    @Override
    public Map2D<R, C, V> putAll(Map2D<? extends R, ? extends C, ? extends V> source) {
        var sourceView = source.rowMapView();
        for (var row : sourceView.entrySet()) {
            for (var colEntry : row.getValue().entrySet()) {
                this.put(row.getKey(), colEntry.getKey(), colEntry.getValue());
            }
        }
        return this;
    }

    @Override
    public Map2D<R, C, V> putAllToRow(Map<? extends C, ? extends V> source, R rowKey) {
        for(var entry : source.entrySet()){
            this.put(rowKey, entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public Map2D<R, C, V> putAllToColumn(Map<? extends R, ? extends V> source, C columnKey) {
        for(var entry : source.entrySet()){
            this.put(entry.getKey(), columnKey, entry.getValue());
        }
        return this;
    }

    @Override
    public <R2, C2, V2> Map2D<R2, C2, V2> copyWithConversion(Function<? super R, ? extends R2> rowFunction, Function<? super C, ? extends C2> columnFunction, Function<? super V, ? extends V2> valueFunction) {
        Map2D<R2, C2, V2> result = new Implementation<>();


        for(var row : map2D.entrySet()){

            Map<C, V> oldRow = row.getValue();
            R2 newRow = rowFunction.apply(row.getKey());

            for(var column : oldRow.entrySet()){

                C2 newCol = columnFunction.apply(column.getKey());
                V2 newValue = valueFunction.apply(column.getValue());
                result.put(newRow, newCol, newValue);
            }
        }
        return result;
    }
}
