function generateChartData(data, scale)
{
    result = [];
    result.push(["Activity", "Duration ( in " + scale + ")"]);
    $.each(data.results.bindings, function(index, value) {
        name = value.an.value + " (" + value.wn.value + ")";

        start = parseDateTime(value.start.value);
        end = parseDateTime(value.end.value);

        startDate = new Date(start.year, start.month, start.day, start.hours, start.minutes, start.seconds);
        endDate = new Date(end.year, end.month, end.day, end.hours, end.minutes, end.seconds);

        value = datediff(startDate, endDate, scale);


        result.push([name, value]);
    });

    return sortChartData(result);
}



function generateColumnData(data)
{
    var columns = [];
    var firstRow = data[0];

    for (var key in firstRow) {
        //var value = firstRow[key];
        var row = {data : key};
        columns.push(row);
    }
    return columns;
}

function generateTableData(result)
{
    var data = [];
    $.each(result.results.bindings, function(i, binding) {

        var row = {};
        for (var key in binding) {
            row[key] = binding[key].value;
        }

        data.push(row);
    });

    return data;
}

function urlencode(str) {
  //       discuss at: http://phpjs.org/functions/urlencode/
  //      original by: Philip Peterson
  //      improved by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
  //      improved by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
  //      improved by: Brett Zamir (http://brett-zamir.me)
  //      improved by: Lars Fischer
  //         input by: AJ
  //         input by: travc
  //         input by: Brett Zamir (http://brett-zamir.me)
  //         input by: Ratheous
  //      bugfixed by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
  //      bugfixed by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
  //      bugfixed by: Joris
  // reimplemented by: Brett Zamir (http://brett-zamir.me)
  // reimplemented by: Brett Zamir (http://brett-zamir.me)
  //             note: This reflects PHP 5.3/6.0+ behavior
  //             note: Please be aware that this function expects to encode into UTF-8 encoded strings, as found on
  //             note: pages served as UTF-8
  //        example 1: urlencode('Kevin van Zonneveld!');
  //        returns 1: 'Kevin+van+Zonneveld%21'
  //        example 2: urlencode('http://kevin.vanzonneveld.net/');
  //        returns 2: 'http%3A%2F%2Fkevin.vanzonneveld.net%2F'
  //        example 3: urlencode('http://www.google.nl/search?q=php.js&ie=utf-8&oe=utf-8&aq=t&rls=com.ubuntu:en-US:unofficial&client=firefox-a');
  //        returns 3: 'http%3A%2F%2Fwww.google.nl%2Fsearch%3Fq%3Dphp.js%26ie%3Dutf-8%26oe%3Dutf-8%26aq%3Dt%26rls%3Dcom.ubuntu%3Aen-US%3Aunofficial%26client%3Dfirefox-a'

  str = (str + '')
    .toString();

  // Tilde should be allowed unescaped in future versions of PHP (as reflected below), but if you want to reflect current
  // PHP behavior, you would need to add ".replace(/~/g, '%7E');" to the following.
  return encodeURIComponent(str)
    .replace(/!/g, '%21')
    .replace(/'/g, '%27')
    .replace(/\(/g, '%28')
    .
  replace(/\)/g, '%29')
    .replace(/\*/g, '%2A')
    .replace(/%20/g, '+');
}

function datediff(fromDate, toDate, interval) {
                    /*
                      * DateFormat month/day/year hh:mm:ss
                      * ex.
                      * datediff('01/01/2011 12:00:00','01/01/2011 13:30:00','seconds');
                      */
                    var second = 1000, minute = second * 60, hour = minute * 60, day = hour * 24, week = day * 7;
                    //fromDate = new Date(fromDate); 
                    //toDate = new Date(toDate); 
                    var timediff = toDate - fromDate;
                    if (isNaN(timediff))
        return NaN;
                    switch (interval) {
                            case "years":
            return toDate.getFullYear() - fromDate.getFullYear();
                            case "months":
            return (
                                            (toDate.getFullYear() * 12 + toDate.getMonth())
                                            -
                                            (fromDate.getFullYear() * 12 + fromDate.getMonth())
                                        );
                            case "weeks"  :
            return Math.floor(timediff / week);
                            case "days"   :
            return Math.floor(timediff / day);  
                            case "hours"  :
            return Math.floor(timediff / hour);  
                            case "minutes":
            return Math.floor(timediff / minute);
                            case "seconds":
            return Math.floor(timediff / second);
                            default:
            return undefined;
                    }
            }

function parseDateTime(dateTime)
{
    dateTimeParts = {};

    parts = dateTime.split("T");
    date = parts[0];
    time = parts[1];

    partsDate = date.split("-");
    partsTime = time.split(":");

    dateTimeParts.year = partsDate[0];
    dateTimeParts.month = partsDate[1];
    dateTimeParts.day = partsDate[2];

    dateTimeParts.hours = partsTime[0];
    dateTimeParts.minutes = partsTime[1];
    dateTimeParts.seconds = partsTime[2];

    return dateTimeParts;
}

function sortChartData(unsortedData)
{
    sortedData = [];

    while (unsortedData.length > 1)
    {
        currentBiggest = unsortedData[1];
        i = 2;
        counter = 1;
        while (i < unsortedData.length)
        {
            if (unsortedData[i][1] > currentBiggest[1])
            {
                currentBiggest = unsortedData[i];
                counter = i;
            }
            i++;
        }

        sortedData.push(currentBiggest);
        unsortedData.splice(counter, 1);
    }

    sortedData.unshift(unsortedData[0]);

    return sortedData;
}
