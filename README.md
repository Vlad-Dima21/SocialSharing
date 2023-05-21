# SocialSharing
Proiect Programare pe dispozitive mobile în Android

Aplicația permite utilizatorilor să încarce poze pe care prietenii să le poată vedea.
Ce am implementat în aplicație:
- RecyclerView pentru fragmentul de Home și cel de Friends. Cel din primul fragment are și o funcție de căutare
- am folosit Bottom Naviagation pentru a naviga între fragmente
- un utilizator poate partaja un link care să permită altora să îl adauge la prieteni (Android Spreadsheet)
- link-ul partajat poate fi deschis în aplicație și declanșează un alert care întreabă utilizatorul dacă vrea să adauge prietenul (deep links)
- cand un utilizator adauga un prieten, ambii primesc o notificare care deschide fragmentul de prieteni
- utilizatorii pot face poze cu camera si le pot incarca
- utilizatorii se pot loga în aplicație cu contul de Google
- utilizatorii se loghează în aplicație cu Firebase Authentication
- datele sunt persistate cu Firebase Firestore (datele de utilizator și detaliile postărilor)
- am folosit dependency injection cu Dagger Hilt

Extra:
- parțial MVVM: am folosit viewModele pentru toate fragmentele, astfel postările nu sunt afectate de lifecycle-ul activității și a fragmentelor. În activitatea în care se fac pozele, informațiile sunt salvate în bundle.
- imaginile sunt incarcate fără a folosi alte librării

