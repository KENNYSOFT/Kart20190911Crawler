var nam=[];
var cnt=[];
for(var i=2070;i<=2088;++i)
{
	nam.push(`Map.entry(${i}l, "${GetItemData(i).replace(/(^\d+ | \(\d+개\)$)/,"")}")`);
	var res=/(?:^(\d+) | \((\d+)개\)$)/.exec(GetItemData(i));
	if(res!==null)cnt.push(`Map.entry(${i}l, ${res[1]||res[2]})`);
}
console.log("public static final Map<Long, String> nameMap = Map.ofEntries("+nam.join(", ")+");");
console.log("public static final Map<Long, Integer> cntMap = Map.ofEntries("+cnt.join(", ")+");");