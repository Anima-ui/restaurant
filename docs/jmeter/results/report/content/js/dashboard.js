/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
var showControllersOnly = false;
var seriesFilter = "";
var filtersOnlySampleSeries = true;

/*
 * Add header in statistics table to group metrics by category
 * format
 *
 */
function summaryTableHeader(header) {
    var newRow = header.insertRow(-1);
    newRow.className = "tablesorter-no-sort";
    var cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Requests";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 3;
    cell.innerHTML = "Executions";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 7;
    cell.innerHTML = "Response Times (ms)";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Throughput";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 2;
    cell.innerHTML = "Network (KB/sec)";
    newRow.appendChild(cell);
}

/*
 * Populates the table identified by id parameter with the specified data and
 * format
 *
 */
function createTable(table, info, formatter, defaultSorts, seriesIndex, headerCreator) {
    var tableRef = table[0];

    // Create header and populate it with data.titles array
    var header = tableRef.createTHead();

    // Call callback is available
    if(headerCreator) {
        headerCreator(header);
    }

    var newRow = header.insertRow(-1);
    for (var index = 0; index < info.titles.length; index++) {
        var cell = document.createElement('th');
        cell.innerHTML = info.titles[index];
        newRow.appendChild(cell);
    }

    var tBody;

    // Create overall body if defined
    if(info.overall){
        tBody = document.createElement('tbody');
        tBody.className = "tablesorter-no-sort";
        tableRef.appendChild(tBody);
        var newRow = tBody.insertRow(-1);
        var data = info.overall.data;
        for(var index=0;index < data.length; index++){
            var cell = newRow.insertCell(-1);
            cell.innerHTML = formatter ? formatter(index, data[index]): data[index];
        }
    }

    // Create regular body
    tBody = document.createElement('tbody');
    tableRef.appendChild(tBody);

    var regexp;
    if(seriesFilter) {
        regexp = new RegExp(seriesFilter, 'i');
    }
    // Populate body with data.items array
    for(var index=0; index < info.items.length; index++){
        var item = info.items[index];
        if((!regexp || filtersOnlySampleSeries && !info.supportsControllersDiscrimination || regexp.test(item.data[seriesIndex]))
                &&
                (!showControllersOnly || !info.supportsControllersDiscrimination || item.isController)){
            if(item.data.length > 0) {
                var newRow = tBody.insertRow(-1);
                for(var col=0; col < item.data.length; col++){
                    var cell = newRow.insertCell(-1);
                    cell.innerHTML = formatter ? formatter(col, item.data[col]) : item.data[col];
                }
            }
        }
    }

    // Add support of columns sort
    table.tablesorter({sortList : defaultSorts});
}

$(document).ready(function() {

    // Customize table sorter default options
    $.extend( $.tablesorter.defaults, {
        theme: 'blue',
        cssInfoBlock: "tablesorter-no-sort",
        widthFixed: true,
        widgets: ['zebra']
    });

    var data = {"OkPercent": 100.0, "KoPercent": 0.0};
    var dataset = [
        {
            "label" : "FAIL",
            "data" : data.KoPercent,
            "color" : "#FF6347"
        },
        {
            "label" : "PASS",
            "data" : data.OkPercent,
            "color" : "#9ACD32"
        }];
    $.plot($("#flot-requests-summary"), dataset, {
        series : {
            pie : {
                show : true,
                radius : 1,
                label : {
                    show : true,
                    radius : 3 / 4,
                    formatter : function(label, series) {
                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'
                            + label
                            + '<br/>'
                            + Math.round10(series.percent, -2)
                            + '%</div>';
                    },
                    background : {
                        opacity : 0.5,
                        color : '#000'
                    }
                }
            }
        },
        legend : {
            show : true
        }
    });

    // Creates APDEX table
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [1.0, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [1.0, 500, 1500, "POST Create Customer"], "isController": false}, {"data": [1.0, 500, 1500, "GET Search JPQL"], "isController": false}, {"data": [1.0, 500, 1500, "POST Create Main Restaurant"], "isController": false}, {"data": [1.0, 500, 1500, "POST Customer Bulk"], "isController": false}, {"data": [1.0, 500, 1500, "POST Transaction Rollback Expected 409"], "isController": false}, {"data": [1.0, 500, 1500, "POST Create Deletable Restaurant"], "isController": false}, {"data": [1.0, 500, 1500, "POST Transaction Cascade"], "isController": false}, {"data": [1.0, 500, 1500, "DELETE Restaurant"], "isController": false}, {"data": [1.0, 500, 1500, "GET Restaurants By City"], "isController": false}, {"data": [1.0, 500, 1500, "GET Async Task Status"], "isController": false}, {"data": [1.0, 500, 1500, "POST Transaction Exception With TX Expected 409"], "isController": false}, {"data": [1.0, 500, 1500, "PUT Booking Status"], "isController": false}, {"data": [1.0, 500, 1500, "GET Concurrency Unsafe"], "isController": false}, {"data": [1.0, 500, 1500, "GET Search Native"], "isController": false}, {"data": [1.0, 500, 1500, "PUT Update Restaurant"], "isController": false}, {"data": [1.0, 500, 1500, "POST Transaction Partial"], "isController": false}, {"data": [1.0, 500, 1500, "POST Transaction Exception No TX Expected 409"], "isController": false}, {"data": [1.0, 500, 1500, "GET Nested NPlusOne Problem"], "isController": false}, {"data": [1.0, 500, 1500, "POST Transaction Customers Bulk With TX"], "isController": false}, {"data": [1.0, 500, 1500, "GET Restaurants All"], "isController": false}, {"data": [1.0, 500, 1500, "GET Transaction State"], "isController": false}, {"data": [1.0, 500, 1500, "GET Customer By ID"], "isController": false}, {"data": [1.0, 500, 1500, "GET Customers All"], "isController": false}, {"data": [1.0, 500, 1500, "GET Concurrency Atomic"], "isController": false}, {"data": [1.0, 500, 1500, "POST Async Customer Bulk"], "isController": false}, {"data": [1.0, 500, 1500, "GET Booking By ID"], "isController": false}, {"data": [1.0, 500, 1500, "POST Transaction Customers Bulk No TX"], "isController": false}, {"data": [1.0, 500, 1500, "GET Bookings All"], "isController": false}, {"data": [1.0, 500, 1500, "GET Booking Statuses"], "isController": false}, {"data": [1.0, 500, 1500, "POST Create Booking"], "isController": false}, {"data": [1.0, 500, 1500, "GET Restaurant By ID"], "isController": false}, {"data": [1.0, 500, 1500, "GET NPlusOne Optimized"], "isController": false}, {"data": [1.0, 500, 1500, "GET NPlusOne Problem"], "isController": false}]}, function(index, item){
        switch(index){
            case 0:
                item = item.toFixed(3);
                break;
            case 1:
            case 2:
                item = formatDuration(item);
                break;
        }
        return item;
    }, [[0, 0]], 3);

    // Create statistics table
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 309, 0, 0.0, 34.98058252427184, 7, 331, 19.0, 84.0, 123.0, 261.69999999999857, 7.7610890641482895, 19.412362210842918, 2.011330224606922], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["POST Create Customer", 10, 0, 0.0, 16.400000000000006, 14, 23, 15.5, 22.8, 23.0, 23.0, 1.139211665527455, 0.22939984905445432, 0.2850254186602871], "isController": false}, {"data": ["GET Search JPQL", 10, 0, 0.0, 23.1, 14, 63, 19.0, 59.500000000000014, 63.0, 63.0, 1.1289230074508918, 0.660045121641454, 0.2667962576202303], "isController": false}, {"data": ["POST Create Main Restaurant", 10, 0, 0.0, 31.900000000000002, 19, 67, 30.0, 63.90000000000001, 67.0, 67.0, 1.1096316023080337, 0.4546888870395029, 0.5386697944407457], "isController": false}, {"data": ["POST Customer Bulk", 10, 0, 0.0, 22.7, 17, 39, 20.0, 38.0, 39.0, 39.0, 1.1375270162666364, 0.4601207911500398, 0.3859149271982709], "isController": false}, {"data": ["POST Transaction Rollback Expected 409", 3, 0, 0.0, 59.66666666666667, 18, 135, 26.0, 135.0, 135.0, 135.0, 1.6181229773462784, 0.5230456108414239, 0.6557822613268608], "isController": false}, {"data": ["POST Create Deletable Restaurant", 10, 0, 0.0, 19.0, 16, 26, 18.5, 25.400000000000002, 26.0, 26.0, 1.1154489682097044, 0.36513524818739546, 0.4467242401003904], "isController": false}, {"data": ["POST Transaction Cascade", 10, 0, 0.0, 21.299999999999997, 16, 28, 20.0, 27.9, 28.0, 28.0, 1.142334932602239, 0.2943966686657528, 0.45972287668494405], "isController": false}, {"data": ["DELETE Restaurant", 10, 0, 0.0, 25.1, 18, 40, 21.0, 39.6, 40.0, 40.0, 1.149029070435482, 0.08191320521659198, 0.22464416005974952], "isController": false}, {"data": ["GET Restaurants By City", 10, 0, 0.0, 66.3, 49, 102, 60.0, 100.60000000000001, 102.0, 102.0, 1.1196954428395476, 10.154784773541598, 0.20447563262792523], "isController": false}, {"data": ["GET Async Task Status", 10, 0, 0.0, 8.400000000000002, 7, 10, 8.5, 10.0, 10.0, 10.0, 1.1422044545973729, 0.40445638206739004, 0.20769381781838947], "isController": false}, {"data": ["POST Transaction Exception With TX Expected 409", 3, 0, 0.0, 15.666666666666666, 13, 17, 17.0, 17.0, 17.0, 17.0, 3.5252643948296125, 1.1704979435957696, 1.466565070505288], "isController": false}, {"data": ["PUT Booking Status", 10, 0, 0.0, 20.3, 16, 30, 18.0, 30.0, 30.0, 30.0, 1.1403808872163301, 0.3952355242901129, 0.2550265851294332], "isController": false}, {"data": ["GET Concurrency Unsafe", 10, 0, 0.0, 17.5, 12, 35, 13.0, 34.400000000000006, 35.0, 35.0, 1.1481056257175661, 0.36573442881745116, 0.26123887772675086], "isController": false}, {"data": ["GET Search Native", 10, 0, 0.0, 18.0, 12, 30, 16.0, 29.300000000000004, 30.0, 30.0, 1.1349449551696742, 0.6636767960503915, 0.27043610259902395], "isController": false}, {"data": ["PUT Update Restaurant", 10, 0, 0.0, 21.799999999999997, 16, 36, 19.5, 35.1, 36.0, 36.0, 1.1362345188046814, 0.4866723241677082, 0.33964979121690714], "isController": false}, {"data": ["POST Transaction Partial", 10, 0, 0.0, 26.5, 18, 36, 25.0, 36.0, 36.0, 36.0, 1.1419435879867537, 0.3230674389060181, 0.4595653834075597], "isController": false}, {"data": ["POST Transaction Exception No TX Expected 409", 3, 0, 0.0, 17.666666666666668, 14, 20, 19.0, 20.0, 20.0, 20.0, 3.4965034965034967, 1.1643629807692308, 1.447770979020979], "isController": false}, {"data": ["GET Nested NPlusOne Problem", 10, 0, 0.0, 210.7, 153, 331, 175.0, 327.6, 331.0, 331.0, 1.1247328759419637, 17.07364276374986, 0.20539555449330785], "isController": false}, {"data": ["POST Transaction Customers Bulk With TX", 10, 0, 0.0, 26.4, 16, 56, 21.5, 54.300000000000004, 56.0, 56.0, 1.1457378551787352, 0.3345465026351971, 0.4144348648029331], "isController": false}, {"data": ["GET Restaurants All", 10, 0, 0.0, 90.9, 70, 156, 87.5, 149.70000000000002, 156.0, 156.0, 1.1064394777605664, 13.80823502157557, 0.19341080714759903], "isController": false}, {"data": ["GET Transaction State", 10, 0, 0.0, 14.0, 11, 21, 13.0, 20.5, 21.0, 21.0, 1.1479738261967627, 0.2645720927562851, 0.20403441051544025], "isController": false}, {"data": ["GET Customer By ID", 10, 0, 0.0, 12.1, 10, 18, 11.0, 17.6, 18.0, 18.0, 1.1419435879867537, 0.22994996859655134, 0.19738673347036656], "isController": false}, {"data": ["GET Customers All", 10, 0, 0.0, 11.4, 9, 13, 11.5, 12.9, 13.0, 13.0, 1.1419435879867537, 1.9273643585131894, 0.1929260163297933], "isController": false}, {"data": ["GET Concurrency Atomic", 10, 0, 0.0, 16.3, 14, 28, 15.0, 26.900000000000006, 28.0, 28.0, 1.1500862564692351, 0.354909430707303, 0.26168954859114435], "isController": false}, {"data": ["POST Async Customer Bulk", 10, 0, 0.0, 9.4, 7, 12, 9.5, 11.9, 12.0, 12.0, 1.141682840506907, 0.2566556541842676, 0.4667074580431556], "isController": false}, {"data": ["GET Booking By ID", 10, 0, 0.0, 14.700000000000001, 12, 22, 14.5, 21.300000000000004, 22.0, 22.0, 1.142465440420427, 0.3937266151605164, 0.19524555866560037], "isController": false}, {"data": ["POST Transaction Customers Bulk No TX", 10, 0, 0.0, 28.399999999999995, 20, 60, 23.5, 57.60000000000001, 60.0, 60.0, 1.1429877700308608, 0.48152824608526684, 0.4156725054291919], "isController": false}, {"data": ["GET Bookings All", 10, 0, 0.0, 34.1, 19, 44, 37.0, 44.0, 44.0, 44.0, 1.141422212076247, 1.5938668174295174, 0.19172326218468214], "isController": false}, {"data": ["GET Booking Statuses", 10, 0, 0.0, 8.800000000000002, 7, 12, 8.0, 12.0, 12.0, 12.0, 1.1427265455376527, 0.19863801279853732, 0.20198584447491716], "isController": false}, {"data": ["POST Create Booking", 10, 0, 0.0, 21.9, 14, 46, 18.5, 44.00000000000001, 46.0, 46.0, 1.1411617026132603, 0.39327730942599565, 0.3317615613944996], "isController": false}, {"data": ["GET Restaurant By ID", 10, 0, 0.0, 15.4, 11, 28, 14.5, 26.800000000000004, 28.0, 28.0, 1.1238480557428636, 0.46051430096650936, 0.19535640031467746], "isController": false}, {"data": ["GET NPlusOne Optimized", 10, 0, 0.0, 99.3, 73, 183, 86.5, 178.3, 183.0, 183.0, 1.1336583153837432, 16.936567388901487, 0.2014900521482825], "isController": false}, {"data": ["GET NPlusOne Problem", 10, 0, 0.0, 100.9, 75, 191, 87.5, 183.00000000000003, 191.0, 191.0, 1.1337868480725624, 16.684160643424036, 0.19929846938775508], "isController": false}]}, function(index, item){
        switch(index){
            // Errors pct
            case 3:
                item = item.toFixed(2) + '%';
                break;
            // Mean
            case 4:
            // Mean
            case 7:
            // Median
            case 8:
            // Percentile 1
            case 9:
            // Percentile 2
            case 10:
            // Percentile 3
            case 11:
            // Throughput
            case 12:
            // Kbytes/s
            case 13:
            // Sent Kbytes/s
                item = item.toFixed(2);
                break;
        }
        return item;
    }, [[0, 0]], 0, summaryTableHeader);

    // Create error table
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": []}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 309, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
