fromStream('dataset20M-1800').
  when({
    "$any": function(s, e) {
      s[e.streamId] = {
        timeArrivedMillis: new Date().getTime()
      }
    }
}).outputState();
