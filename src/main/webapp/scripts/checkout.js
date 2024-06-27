function addElementToPage(item){
        var radio = document.createElement('input');
        radio.name = "g1";
        radio.type = "radio";
        radio.id = "radio_" + item['id'];
        radio.value = item['id'];

        var outerDiv = document.createElement('div');
        outerDiv.id = "shipment-selection-" + item['id'];

        var label = document.createElement('label');
        label.for = "radio_" + item['id'];
        label.class = "shipment-option";
        label.innerHTML = "<strong>" + item['via'] + "</strong><br>" +
            item['citta'] + "<br>" +
            item['destinatario'] + "<br>";

        var outerForm = document.getElementById("shipment-info-form");
        if (outerForm == null) {
            outerForm = document.createElement("form");
            outerForm.id = "shipment-info-form";
            outerForm.name = "shipment-selection";
            document.getElementById("shipment-options").appendChild(outerForm);
        }

        outerDiv.appendChild(label);
        outerForm.appendChild(radio);
        outerForm.appendChild(outerDiv);
}

/**
 * Modifica "shipment-info-form" per contenere tutte le informazioni di spedizione
 *  tranne quella di default (giò nella pagina)
 * @param infos array di object
 */
export function updatePage(infos) {
    let isFirstShipmentInfo = document.getElementById("default-shipment-info") != null;
    if (isFirstShipmentInfo){
        addElementToPage(infos[0])
    } else {
        infos.forEach(function (item) {
            if (item['isDefault'] !== 'true')
                addElementToPage(item);
        });
    }
}

document.addEventListener('DOMContentLoaded', function () {
    function retrieveShipmentInfos() {

        return new Promise(function (resolve, reject) {
            const xhr = new XMLHttpRequest();
            xhr.open('GET', 'getShipmentInfos', true);
            xhr.setRequestHeader('Accept', 'application/json');

            xhr.onreadystatechange = function () {
                if (xhr.readyState === XMLHttpRequest.DONE) {
                    if (xhr.status === 200) {
                        resolve(JSON.parse(xhr.responseText));
                    } else {
                        reject("error: " + xhr.responseText);
                    }
                }
            }
            xhr.send();
        })
    }

    var addShipmentInfosBtn = document.getElementById("add-shipment-infos-btn");
    var getShipmentInfosBtn = document.getElementById("get-shipment-infos-btn");
    if (getShipmentInfosBtn != null){
        getShipmentInfosBtn.addEventListener('click', function () {
            retrieveShipmentInfos().then(function (shipmentInfos) {
                updatePage(shipmentInfos);
                getShipmentInfosBtn.hidden = true;
                addShipmentInfosBtn.hidden = false;
            }).catch(function (err) {
                console.error(err);
            });
        });
    }

    let submitForm = document.getElementById('submit-form');
    submitForm.addEventListener("submit", function (){
        let shipmentInfoForm = document.getElementById("shipment-info-form");
        let radioSelections = shipmentInfoForm.querySelectorAll("input");
        let option = document.getElementById("selected-option");
        radioSelections.forEach(function(radio) {
            if (radio.checked) {
                option.value = radio.value;
            }
        })
    })
});