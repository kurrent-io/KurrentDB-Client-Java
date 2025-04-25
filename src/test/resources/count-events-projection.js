fromStream('dataset20M-1800').
  when({
    "$init": function() {
      return {
        count: 0
      }
    },
    "$any": function(s, e) {
      s.count = s.count + 1;
    }
}).outputState();
