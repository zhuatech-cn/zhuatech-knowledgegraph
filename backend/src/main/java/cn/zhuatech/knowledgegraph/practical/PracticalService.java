/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.knowledgegraph.practical;

import static cn.zhuatech.knowledgegraph.practical.PracticalInput.*;

import java.math.*;
import java.util.*;
import org.springframework.stereotype.Service;

/** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
@Service
public class PracticalService {
  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  public Map<String, Object> execute(Map<String, Object> r) {
    validate(r);
    keys(r, "source", "target", "maxDepth", "allowedTypes", "nodes", "edges");
    var ns = strings(r, "nodes", 1, 200);
    Set<String> nodes = new HashSet<>(ns);
    if (nodes.size() != ns.size()) throw new IllegalArgumentException("节点重复");
    String source = id(r, "source"), target = id(r, "target");
    if (!nodes.containsAll(List.of(source, target))) throw new IllegalArgumentException("起终点未知");
    int max = integer(r, "maxDepth", 0, 20);
    Set<String> types = new HashSet<>(strings(r, "allowedTypes", 0, 100)), ids = new HashSet<>();
    Map<String, List<Map<String, Object>>> graph = new TreeMap<>();
    for (var e : rows(r, "edges", 0, 500)) {
      keys(e, "id", "from", "to", "type");
      unique(ids, id(e, "id"));
      String from = id(e, "from"), to = id(e, "to"), type = id(e, "type");
      if (!nodes.containsAll(List.of(from, to))) throw new IllegalArgumentException("边引用未知节点");
      if (types.contains(type)) graph.computeIfAbsent(from, k -> new ArrayList<>()).add(e);
    }
    graph.values().forEach(l -> l.sort(Comparator.comparing(e -> e.get("id").toString())));
    Map<String, List<Map<String, Object>>> paths = new HashMap<>();
    Deque<String> queue = new ArrayDeque<>();
    paths.put(source, List.of());
    queue.add(source);
    while (!queue.isEmpty()) {
      String current = queue.remove();
      var path = paths.get(current);
      if (current.equals(target)) break;
      if (path.size() >= max) continue;
      for (var edge : graph.getOrDefault(current, List.of())) {
        String next = edge.get("to").toString();
        if (!paths.containsKey(next)) {
          var copy = new ArrayList<>(path);
          copy.add(edge);
          paths.put(next, List.copyOf(copy));
          queue.add(next);
        }
      }
    }
    boolean found = paths.containsKey(target);
    var path = paths.getOrDefault(target, List.of());
    List<String> ordered = new ArrayList<>();
    if (found) {
      ordered.add(source);
      for (var e : path) ordered.add(e.get("to").toString());
    }
    return Map.of(
        "found",
        found,
        "hopCount",
        path.size(),
        "nodes",
        ordered,
        "edges",
        path,
        "visitedNodes",
        paths.size(),
        "automaticWrite",
        false);
  }
}
