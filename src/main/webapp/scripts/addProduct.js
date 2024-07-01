import {showNotification} from "./notification.js";

document.addEventListener("DOMContentLoaded", function () {
    let categorySelect = document.getElementById('category');
    let sterilizedLabel = document.querySelector('label[for="sterilized"]');
    let sterilizedSelect = document.getElementById('sterilized');

    categorySelect.addEventListener('change', function() {
        if (this.value === 'Alimentari') {
            sterilizedLabel.style.display = 'block';
            sterilizedSelect.style.display = 'block';
        } else {
            sterilizedLabel.style.display = 'none';
            sterilizedSelect.style.display = 'none';
        }
    });

    let minAgeInput = document.getElementById('minEta');
    let maxAgeInput = document.getElementById('maxEta');

    function validateAge() {
        let minAge = parseInt(minAgeInput.value);
        let maxAge = parseInt(maxAgeInput.value);

        if (maxAge < minAge) {
            alert("L'età massima deve essere maggiore o uguale all'età minima."); //todo cambiare alert con un messaggio più elegante
            maxAgeInput.value = minAge;
        }
    }

    minAgeInput.addEventListener('change', validateAge);
    maxAgeInput.addEventListener('change', validateAge);

    const form = document.getElementsByClassName("form-container")[0];
    form.addEventListener("submit", function(event) {
        event.preventDefault(); // Impedisce l'invio classico del form

        const formData = new FormData(form);

        const xhr = new XMLHttpRequest();
        xhr.open("POST", "addProductServlet", true);
        xhr.onreadystatechange = function () {
            if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                    var response = JSON.parse(xhr.responseText);
                    if (response.status === "success"){
                        showNotification("Operazione effettuata", "success");
                    } else {
                        showNotification("Errore", "error");
                    }
                } else {
                    showNotification("Errore", "error");
                }
            }
        };
        xhr.onerror = function () {
            showNotification("Errore di rete", "error");
        };
        xhr.send(formData);
    })
});