var sigmaGraph;

window.onload = function (event) {
    showGraph();
    //setTimeout(() => { console.log("World!"); }, 2000);
    //sigmaGraph.stopForceAtlas2();    
};

// s = sigma.parsers.json('data2.json', {
//     container: 'container',
//     settings: {
//         defaultNodeColor: '#ec5148'
//     }
// },
//     function (s) {
//         s.startForceAtlas2({ worker: true, barnesHutOptimize: false, linLogMode: true, gravity: 5 });
//     }
// );

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

var forceAtlasConfig = {
    worker: true,
    barnesHutOptimize: false,
    barnesHutTheta: 1.5,
    linLogMode: false,
    scalingRatio: 4,
    gravity: 2
}

function showGraph() {
    var dataFile = 'levy.json';

    var networkSettings = {
        container: 'graph-container',
        settings: {
            // autoRescale: false,
            edgeLabelSize: 'proportional',
            minArrowSize: '5',
            defaultEdgeType: 'curve',
            hoverFontStyle: 'bold',
            labelThreshold: 5,
            defaultLabelSize: 14,
            defaultNodeColor: '#6699ff'
        }
    };

    var f = function (s) {
        //setTimeout(() => { console.log("World!"); }, 2000);
        s.startForceAtlas2(forceAtlasConfig);
        sigmaGraph = s;
        initGraphFuncionality(s);
    }

    sigma.parsers.json(dataFile, networkSettings, f);
}


function initGraphFuncionality(s) {
    // var dragListener = sigma.plugins.dragNodes(s, s.renderers[0]);
    // dragListener.bind('startdrag', function (event) {
    //     console.log(event);
    // });
    // dragListener.bind('drag', function (event) {
    //     console.log(event);
    // });
    // dragListener.bind('drop', function (event) {
    //     console.log(event);
    // });
    // dragListener.bind('dragend', function (event) {
    //     console.log(event);
    // });

    var clickNodeHandler = s._handlers.clickNode;
    if (!clickNodeHandler) {
        s.bind('clickNode', function (clickNodeEvent) {
            var node = clickNodeEvent.data.node;
            //executeHighlight(clickNodeEvent, node);
        });

        s.bind('clickStage', function (clickStageEvent) {
            //resetGraph();
        });
    }


    function executeHighlight(event, node) {
        //resetGraph();
        //highlightNeighbours(event, node);
        //s.refresh();
    }

    function resetGraph() {
        s.graph.nodes().forEach(function (n) {
            n.color = n.originalColor;
            n.size = n.originalSize;
        });

        s.graph.edges().forEach(function (e) {
            e.color = e.originalColor;
        });

        // Same as in the previous event:
        s.refresh();
    }

    function highlightNeighbours(event, node) {
        var nodeId = node.id;

        var toKeep = s.graph.neighbors(nodeId);

        //if (event.type === "clickNode")
        toKeep[nodeId] = node;

        s.graph.nodes().forEach(function (n) {
            if (toKeep[n.id])
                n.color = n.originalColor;
            else
                n.color = '#eee';

            if (n.id === nodeId) {
                n.size = n.size * 2;
                n.color = "#9400D3";
            }
        });

        s.graph.edges().forEach(function (edges) {
            if (toKeep[edges.source] && toKeep[edges.target])
                edges.color = edges.originalColor;
            else
                edges.color = '#eee';
        });

        s.refresh();
    }
   
    //utils.$('stop-btn').addEventListener("click", stopGraph);

    var radios = document.getElementsByName('barnesHutOptimizeValue');
    for (var i = 0, max = radios.length; i < max; i++) {
        radios[i].onclick = function () {
            var currentConfig = forceAtlasConfig;
            currentConfig.barnesHutOptimize = event.target.value === 'True';
            sigmaGraph.configForceAtlas2(currentConfig);    
        }
    }

    var radios = document.getElementsByName('linLogModeValue');
    for (var i = 0, max = radios.length; i < max; i++) {
        radios[i].onclick = function () {
            var currentConfig = forceAtlasConfig;
            currentConfig.linLogMode = event.target.value === 'True';
            sigmaGraph.configForceAtlas2(currentConfig);    
        }
    }    

    var barnesHutThetaValue = document.getElementById('barnesHutThetaValue');
    barnesHutThetaValue.addEventListener('change', (event) => {
        var currentConfig = forceAtlasConfig;
        currentConfig.barnesHutTheta =  parseFloat(event.target.value);
        sigmaGraph.configForceAtlas2(currentConfig);          
    });

    var radios = document.getElementsByName('outboundAttractionDistributionValue');
    for (var i = 0, max = radios.length; i < max; i++) {
        radios[i].onclick = function () {
            var currentConfig = forceAtlasConfig;
            currentConfig.outboundAttractionDistribution = event.target.value === 'True';
            sigmaGraph.configForceAtlas2(currentConfig);    
        }
    }    

    var radios = document.getElementsByName('adjustSizesValue');
    for (var i = 0, max = radios.length; i < max; i++) {
        radios[i].onclick = function () {
            var currentConfig = forceAtlasConfig;
            currentConfig.adjustSizes = event.target.value === 'True';
            sigmaGraph.configForceAtlas2(currentConfig);    
        }
    }    

    var barnesHutThetaValue = document.getElementById('scalingRatioValue');
    barnesHutThetaValue.addEventListener('change', (event) => {
        var currentConfig = forceAtlasConfig;
        currentConfig.scalingRatio =  parseInt(event.target.value);
        sigmaGraph.configForceAtlas2(currentConfig);          
    });    


    var radios = document.getElementsByName('strongGravityModeValue');
    for (var i = 0, max = radios.length; i < max; i++) {
        radios[i].onclick = function () {
            var currentConfig = forceAtlasConfig;
            currentConfig.strongGravityMode = event.target.value === 'True';
            sigmaGraph.configForceAtlas2(currentConfig);    
        }
    }        

    var barnesHutThetaValue = document.getElementById('gravityValue');
    barnesHutThetaValue.addEventListener('change', (event) => {
        var currentConfig = forceAtlasConfig;
        currentConfig.gravity =  parseInt(event.target.value);
        sigmaGraph.configForceAtlas2(currentConfig);          
    });    

    s.refresh();
}

function stopGraph() {
    sigmaGraph.stopForceAtlas2();
}

sigma.classes.graph.addMethod('neighbors', function (nodeId) {
    var k,
        neighbors = {},
        index = this.allNeighborsIndex[nodeId] || {};

    for (k in index)
        neighbors[k] = this.nodesIndex[k];

    return neighbors;
});