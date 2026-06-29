document.getElementById('soap-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const bookId = document.getElementById('soapBookId').value;
    const submitBtn = document.getElementById('soapSubmitBtn');
    
    toggleButton('soapSubmitBtn', true);
    showLoading('soapLoader');
    
    document.getElementById('soapSuccess').style.display = 'none';
    document.getElementById('soapError').style.display = 'none';
    const rawOutput = document.getElementById('rawXmlOutput');
    
    const soapRequest = `<?xml version="1.0" encoding="utf-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:lib="http://smartcampus.com/library">
    <soapenv:Header/>
    <soapenv:Body>
        <lib:getBookAvailabilityRequest>
            <lib:bookId>${bookId}</lib:bookId>
        </lib:getBookAvailabilityRequest>
    </soapenv:Body>
</soapenv:Envelope>`;

    rawOutput.textContent = "REQUEST:\n" + soapRequest + "\n\nWAITING FOR RESPONSE...";

    try {
        const response = await fetch(API.soap, {
            method: 'POST',
            headers: {
                'Content-Type': 'text/xml;charset=UTF-8',
            },
            body: soapRequest
        });
        
        const xmlText = await response.text();
        rawOutput.textContent = "REQUEST:\n" + soapRequest + "\n\nRESPONSE:\n" + xmlText;
        
        const parser = new DOMParser();
        const xmlDoc = parser.parseFromString(xmlText, "text/xml");
        
        // Check for SOAP Fault
        const fault = xmlDoc.getElementsByTagName("SOAP-ENV:Fault")[0] || xmlDoc.getElementsByTagName("env:Fault")[0] || xmlDoc.getElementsByTagName("faultcode")[0];
        
        if (fault || !response.ok) {
            let faultString = "Unknown SOAP Fault";
            const fsNode = xmlDoc.getElementsByTagName("faultstring")[0];
            if (fsNode) {
                faultString = fsNode.textContent;
            }
            
            document.getElementById('soapErrorText').textContent = faultString;
            document.getElementById('soapError').style.display = 'block';
            showToast('SOAP Fault occurred', 'error');
        } else {
            // Parse successful response
            const availableNode = xmlDoc.getElementsByTagName("ns2:available")[0] || xmlDoc.getElementsByTagName("available")[0];
            const titleNode = xmlDoc.getElementsByTagName("ns2:title")[0] || xmlDoc.getElementsByTagName("title")[0];
            
            const isAvailable = availableNode ? availableNode.textContent === 'true' : false;
            const title = titleNode ? titleNode.textContent : 'Book ' + bookId;
            
            if (isAvailable) {
                document.getElementById('soapSuccessText').textContent = `The book "${title}" is currently available in the library!`;
            } else {
                document.getElementById('soapSuccessText').textContent = `The book "${title}" is currently NOT available.`;
            }
            
            document.getElementById('soapSuccess').style.display = 'block';
            showToast('SOAP request successful');
        }
        
    } catch (error) {
        rawOutput.textContent = `Error connecting to SOAP endpoint at ${API.soap}.\nMake sure Library Booking Service is running.`;
        document.getElementById('soapErrorText').textContent = "Connection refused. Is the server running?";
        document.getElementById('soapError').style.display = 'block';
        showToast('Connection failed', 'error');
    } finally {
        hideLoading('soapLoader');
        toggleButton('soapSubmitBtn', false);
    }
});
