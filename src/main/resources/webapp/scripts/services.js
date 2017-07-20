angular.module('cakeReduxModule')
    .factory("eventFactory",["$http", function($http) {
        var res = function (callback) {

            $http({method: "GET", url: "data/events"})
                .success(function(data) {
                    callback(data);
                });

        };
         return res;
    }])
    .factory('talkList',[function() {
        var li = {
            allTalks : [],
            talks: [],
            chosenEvent: null

        };
        return _.clone(li);
    }])
    .factory('slotFilterService',[
        function() {
            var filter = {
                filterValue: {
                    ten: true,
                    sixty: true,
                    others: true
                },
                doFilter: function(allSlots) {
                    var self = this;
                    return _.filter(allSlots,function(slot) {
                        switch (slot.length) {
                            case 10:
                                return self.filterValue.ten;
                            case 60:
                                return self.filterValue.sixty;
                            default:
                                return self.filterValue.others;
                        };
                    });
                }
            };
            return filter;
        }
    ])
    .factory('roomSlotFactory',[
        "$http","$q","slotFilterService",function($http,$q,slotFilterService) {
            var fact = {
                eventsWithSlotsRoom: [],
                roomsSlotsForEvent: function(eventid) {
                    if (this.eventsWithSlotsRoom[eventid]) {
                        return this.eventsWithSlotsRoom[eventid];
                    }
                    var deffered = $q.defer();
                    $http({method: "GET", url: "data/roomsSlots?eventId=" + eventid})
                        .success(function(data) {
                            var roomSl = {
                                rooms: data.rooms,
                                allSlots: data.slots,
                                slots: slotFilterService.doFilter(data.slots)
                            };
                            deffered.resolve(roomSl);
                        });
                    this.eventsWithSlotsRoom[eventid] = deffered.promise;
                    return this.eventsWithSlotsRoom[eventid];
                }
            };
            return fact;
        }
    ])
    .factory('cookieService',[function() {
        return {
            setCookie : function(cname,cvalue,exdays) {
                var d = new Date();
                d.setTime(d.getTime()+(exdays*24*60*60*1000));
                var expires = "expires="+d.toGMTString();
                document.cookie = cname + "=" + cvalue + "; " + expires;
            },

            getCookie : function(cname) {
                var name = cname + "=";
                var ca = document.cookie.split(';');
                for(var i=0; i<ca.length; i++)
                {
                    var c = ca[i].trim();
                    if (c.indexOf(name)==0) return c.substring(name.length,c.length);
                }
                return "[]";
            }

        };
    }])
    .factory('showScheduleService',function() {
       var func = function(talks) {
           var result = {
                rooms : [],
                slots: []
           };
           _.each(talks, function(talk) {
               var roomName = talk.room ? talk.room.name : "No room";
               if (_.indexOf(result.rooms,roomName) == -1) {
                   result.rooms.push(roomName);
               }

           });
           result.rooms.sort();
           _.each(talks,function(talk) {
               var slotName =  talk.slot ? talk.slot.start + "-" + talk.slot.end : "No slot";
               var roomName = talk.room ? talk.room.name : "No room";
               var slot = _.find(result.slots,function(aslot) {
                    return aslot.name === slotName;
               });
               if (!slot) {
                   slot = {
                       name: slotName,
                       rooms: []
                   };
                   _.each(result.rooms,function(rname) {
                        slot.rooms.push({name: rname, talks: []});
                   });
                   result.slots.push(slot);
               }

               var roomSlot = _.find(slot.rooms,function(aroom) {
                   return aroom.name === roomName;
               });
               roomSlot.talks.push(talk);
           });
           result.slots = _.sortBy(result.slots,function(slot) {
               return slot.name;
           });
           return result;
       };
       return func;
    })
    .factory('filterService',["cookieService",function(cookieService) {
        var b = cookieService.a;
        var isMatch= function(filter,obj) {
            if (filter && filter.length > 0 && obj.toLowerCase().indexOf(filter.toLowerCase()) == -1) {
                return false;
            }
            return true;
        }
        var findInArray=function(filter, arr,excactMatch) {
            if (!filter || filter.length == 0) {
                return true;
            }
            if (!arr) {
                return false;
            }
            var found = false;
            _.each(arr, function(item) {
                if (found) {
                   return;
                }
                if (excactMatch) {
                    if (filter.endsWith("*")) {
                        if (item.toLowerCase().startsWith(filter.toLowerCase().substr(0,filter.length-1))) {
                            found = true;
                        }
                    }
                    else if (item.toLowerCase() === filter.toLowerCase()) {
                        found = true;
                    }
                } else {
                    if (item.toLowerCase().indexOf(filter.toLowerCase()) !== -1) {
                        found = true;
                    }
                }
            });
            return found;
        }

        var matchesFilter = function(talk,filter) {
            if (!isMatch(filter.title,talk.title)) {
                return false;
            }
            if (filter.speaker && filter.speaker.length > 0) {
                var foundSpeaker = false;
                _.each(talk.speakers,function(speaker) {
                    if (foundSpeaker) {
                        return;
                    }

                    if (isMatch(filter.speaker,speaker.name)) {
                        foundSpeaker = true;
                        return;
                    }
                });
                if (!foundSpeaker) {
                    return false;
                }
            }
            if (!isMatch(filter.format,talk.format)) {
                return false;
            }
            if (filter["length"] && filter["length"] !== talk["length"]) {
                return false;
            }
            if (!isMatch(filter.language,talk.lang)) {
                return false;
            }
            if (!findInArray(filter.tag,talk.tags,true)) {
                return false;
            }
            if (!isMatch(filter.hasUnpublishedValues,talk.hasUnpublishedValues)) {
                return false;
            }
            if (!findInArray(filter.keyword,talk.keywords)) {
                return false;
            }
            if (!isMatch(filter.state,talk.state)) {
                return false;
            }
            if (filter.room && filter.room.length > 0 && (!talk.room || !isMatch(filter.room,talk.room.name))) {
                return false;
            }
            if (filter.slot && filter.slot.length > 0 && (!talk.slot || !isMatch(filter.slot,talk.slot.start+"-"+talk.slot.end))) {
                return false;
            }
            return true;
        }

        var filterOperators = {
            OP_AND: 1,
            OP_OR: 2,
            OP_NOT: 3,
            OP_END: 4
        };

        var doMathing = function myself(talk,filters,from,operator) {
            var match = operator == filterOperators.OP_AND;
            for (var num=from;num<filters.length;num++) {
                var matchesThis;
                if (filters[num].filterOperator) {
                    var useOper = filters[num].filterOperator;
                    if (useOper == filterOperators.OP_END) {
                        return {
                            num: num,
                            match: match
                        };
                    }
                    if (useOper == filterOperators.OP_NOT) {
                        useOper = filterOperators.OP_OR;
                    }
                    var res = myself(talk,filters,num+1,useOper);
                    matchesThis = res.match;
                    if (filters[num].filterOperator == filterOperators.OP_NOT) {
                        matchesThis = !matchesThis;
                    }
                    num=res.num;
                } else {
                    matchesThis=matchesFilter(talk,filters[num]);
                }
                if (operator == filterOperators.OP_AND) {
                    match = match && matchesThis;
                } else {
                    match = match || matchesThis;
                }

            }
            return {
                num: num,
                match: match
            };

        }

        var myFilt = JSON.parse(cookieService.getCookie("cakeFilter"));

        var fis = {
            filters : myFilt,
            filterOperators : filterOperators,
            filteredTalks: [],
            usedTags: "",
            doFilter : function(talks,allTalks) {
                var self = this;
                cookieService.setCookie("cakeFilter",JSON.stringify(self.filters),1);
                talks.splice(0,talks.length);
                var allTags = [];
                var allKeywords = [];
                _.each(allTalks,function(talk) {
                    var res;
                    if (self.filters.length == 0) {
                        res = {match: true};
                    } else {
                        res = doMathing(talk,self.filters,0,filterOperators.OP_OR);
                    }
                    if (res.match) {
                        talks.push(talk);
                        if (_.isArray(talk.tags)) {
                            talk.tags.forEach(function(at) {
                                allTags.push(at);
                            });
                        }
                        if (_.isArray(talk.keywords)) {
                            talk.keywords.forEach(function(at) {
                                allKeywords.push(at);
                            });
                        }
                    }
                });
                this.filteredTalks = talks;
                var countedTags = _.countBy(allTags);
                
                this.usedTags = _.map(_.keys(countedTags).sort(),function(key) {
                    return {
                        tagname: key,
                        count: countedTags[key]
                    }; 
                });
                var countedKeywords = _.countBy(allKeywords);
                this.usedKeywords = _.map(_.keys(countedKeywords).sort(),function(key) {
                    return {
                        keywordname: key,
                        count: countedKeywords[key]
                    };
                });
                

            },
            injectFilter: function(filterstr) {
                if (_.isString(filterstr)) {
                    this.filters = JSON.parse(filterstr);
                }
            }
        };
        return fis;
    }])
;
