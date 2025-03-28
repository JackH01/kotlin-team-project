package com.tripwizard.data

val parisTrip = Trip(id = 1, name = "Paris", description = "Paris, the City of Light, enchants with the Eiffel Tower, Louvre treasures, and romantic charm along the Seine—a global center of art, fashion, and timeless elegance.",
    latitude = 48.8566, longitude = 2.3522, radius = 105000f)
val parisAttractions = listOf(
    Attraction(id = 0, name= "Eiffel Tower", description = "Iconic landmark offering panoramic views", tripId = 1, priority = Priority.MEDIUM),
    Attraction(id = 1, name= "Louvre Museum", description = "World's largest art museum, a historic treasure", tripId = 1, priority = Priority.MEDIUM),
    Attraction(id = 2, name= "Seine River", description = "Charming waterway winding through the city", tripId = 1, priority = Priority.MEDIUM),
    Attraction(id = 3, name = "Notre-Dame", description = "Gothic masterpiece on the Île de la Cité", tripId = 1, priority = Priority.MEDIUM),
    Attraction(id = 4, name = "Montmartre", description = "Artistic district with a basilica offering city views", tripId = 1, priority = Priority.MEDIUM),
    Attraction(id = 5, name = "Musée d'Orsay", description = "Renowned museum in a former railway station", tripId = 1, priority = Priority.MEDIUM),
    Attraction(id = 6, name = "Père Lachaise Cemetery", description = "Historic cemetery with notable graves, including Jim Morrison's", tripId = 1, priority = Priority.MEDIUM),
)
val parisLabels = listOf(
    Label(0, LabelOptions.CITY, 1),
    Label(1, LabelOptions.CULTURE, 1)
)

val berlinTrip = Trip(id = 2, name = "Berlin", description = "Berlin, the German capital, boasts a vibrant cultural scene, historical landmarks like the Brandenburg Gate, and a dynamic mix of modernity and history.",
    latitude = 52.5200, longitude = 13.4050, radius = 100000f)
val berlinAttractions = listOf(
    Attraction(id = 7, name= "Brandenburg Gate", description = "Historic symbol and monumental city gate", tripId = 2, priority = Priority.MEDIUM),
    Attraction(id = 8, name= "Berlin Wall", description = "Powerful remnants of Berlin's divided past", tripId = 2, priority = Priority.MEDIUM),
    Attraction(id = 9, name= "Reichstag", description = "German Parliament building with a glass dome for panoramic views", tripId = 2, priority = Priority.MEDIUM),
    Attraction(id = 10, name= "Checkpoint Charlie", description = "Famous Cold War checkpoint and museum", tripId = 2, priority = Priority.MEDIUM),
    Attraction(id = 11, name= "Holocaust Memorial", description = "Powerful memorial dedicated to the Jewish victims of the Holocaust", tripId = 2, priority = Priority.MEDIUM),
)
val berlinLabels = listOf(
    Label(2, LabelOptions.CITY, 2)
)

val lodzTrip = Trip(id = 3, name = "Łódź", description = "Łódź, Poland's city of contrasts, harmonizes industrial heritage with vibrant street art, film festivals, and the lively Piotrkowska Street.",
    latitude = 51.7592, longitude = 19.4550, radius = 50000f)
val lodzAttractions = listOf(
    Attraction(id = 12, name= "Piotrkowska Street", description = "Historic street adorned with vibrant art, shops, and lively festivals", tripId = 3, priority = Priority.MEDIUM),
    Attraction(id = 13, name= "Textile Museum", description = "Showcases the city's rich industrial heritage in the textile industry", tripId = 3, priority = Priority.MEDIUM),
    Attraction(id = 14, name= "Manufaktura", description = "Former industrial complex turned into a modern shopping, arts, and entertainment center", tripId = 3, priority = Priority.MEDIUM),
    Attraction(id = 15, name= "EC1 Łódź - City of Culture", description = "Cultural complex housed in a former power station, featuring exhibitions and events", tripId = 3, priority = Priority.MEDIUM),
    Attraction(id = 16, name= "Film Museum", description = "Celebrates Łódź's significant role in the history of Polish cinema", tripId = 3, priority = Priority.MEDIUM),
)
val lodzLabels = listOf(
    Label(3, LabelOptions.CITY, 3),
    Label(4, LabelOptions.KIDS, 3)
)

val sheffieldTrip = Trip(id = 4, name = "Sheffield", description = "Sheffield, nestled in South Yorkshire, England, combines industrial history with a contemporary arts scene, picturesque hills, and vibrant markets.",
    latitude = 53.3811, longitude = -1.4701, radius = 40000f)
val sheffieldAttractions = listOf(
    Attraction(id = 17, name = "Sheffield Winter Garden", description = "Largest urban glasshouse in Europe, housing a diverse collection of plants", tripId = 4, priority = Priority.MEDIUM),
    Attraction(id = 18, name = "Chatsworth House", description = "Majestic stately home surrounded by beautiful gardens and landscapes", tripId = 4, priority = Priority.MEDIUM),
    Attraction(id = 19, name = "Kelham Island Museum", description = "Industrial museum showcasing Sheffield's manufacturing history", tripId = 4, priority = Priority.MEDIUM),
    Attraction(id = 20, name = "Peak District National Park", description = "Picturesque hills and outdoor adventures on Sheffield's doorstep", tripId = 4, priority = Priority.MEDIUM),
    Attraction(id = 21, name = "Millennium Gallery", description = "Contemporary art and design in the heart of the city", tripId = 4, priority = Priority.MEDIUM),
    Attraction(id = 22, name = "Sheffield Botanical Gardens", description = "Victorian gardens with diverse plant collections and stunning greenhouses", tripId = 4, priority = Priority.MEDIUM),
    Attraction(id = 23, name = "Weston Park Museum", description = "Archaeological and natural history exhibits in a beautiful park setting", tripId = 4, priority = Priority.MEDIUM),
    Attraction(id = 24, name = "Sheffield Cathedral", description = "Historic cathedral with impressive architecture and peaceful surroundings", tripId = 4, priority = Priority.MEDIUM)
)
val sheffieldLabels = listOf(
    Label(5, LabelOptions.ACTIVE, 4)
)

val shanghaiTrip = Trip(id = 5, name = "Shanghai", description = "Shanghai, China's dynamic metropolis, showcases a modern skyline along the Huangpu River, blending tradition with global commerce and iconic landmarks like the Oriental Pearl Tower.",
    latitude = 31.2304, longitude = 121.4737, radius = 150000f)
val shanghaiAttractions = listOf(
    Attraction(id = 25, name = "Oriental Pearl Tower", description = "Iconic skyscraper on the Shanghai skyline.", tripId = 5, priority = Priority.MEDIUM),
    Attraction(id = 26, name = "The Bund", description = "Historic waterfront area with colonial architecture.", tripId = 5, priority = Priority.MEDIUM),
    Attraction(id = 27, name = "Yuyuan Garden", description = "Classical Chinese garden in the Old City.", tripId = 5, priority = Priority.MEDIUM),
    Attraction(id = 28, name = "Shanghai Museum", description = "China's premier museum with ancient artifacts and art collections.", tripId = 5, priority = Priority.MEDIUM),
    Attraction(id = 29, name = "Jin Mao Tower", description = "Skyscraper with panoramic views and a unique design.", tripId = 5, priority = Priority.MEDIUM),
    Attraction(id = 30, name = "Nanjing Road", description = "One of the world's busiest shopping streets.", tripId = 5, priority = Priority.MEDIUM),
    Attraction(id = 31, name = "Zhujiajiao Water Town", description = "Ancient water town with canals, bridges, and traditional architecture.", tripId = 5, priority = Priority.MEDIUM)
)
val shanghaiLabels = listOf(
    Label(6, LabelOptions.CITY, 5),
    Label(7, LabelOptions.BEACH, 5)
)

val templateTrips = listOf(
    TripWithAttractionsAndLabels(parisTrip, parisAttractions, parisLabels),
    TripWithAttractionsAndLabels(berlinTrip, berlinAttractions, berlinLabels),
    TripWithAttractionsAndLabels(lodzTrip, lodzAttractions, lodzLabels),
    TripWithAttractionsAndLabels(sheffieldTrip, sheffieldAttractions, sheffieldLabels),
    TripWithAttractionsAndLabels(shanghaiTrip, shanghaiAttractions, shanghaiLabels),
)