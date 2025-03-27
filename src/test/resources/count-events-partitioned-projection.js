fromStream('dataset20M-1800')
.partitionBy(function(event){
  return event.data.somedata % 2 == 1 ? "odd" : "even";
})
.when({
  "$init": function() {
    return {
      count: 0
    }
  },
  "$any": function(s, e) {
    s.count = s.count + 1;
  }
}).outputState();
