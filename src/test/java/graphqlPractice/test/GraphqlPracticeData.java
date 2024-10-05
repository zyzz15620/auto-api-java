package graphqlPractice.test;

public class GraphqlPracticeData {
    public static final String person10Response = """
            {
                "data": {
                    "person": {
                        "name": "Obi-Wan Kenobi",
                        "birthYear": "57BBY",
                        "gender": "male",
                        "height": 182,
                        "mass": 77,
                        "starshipConnection": {
                            "edges": [
                                {
                                    "node": {
                                        "name": "Jedi starfighter",
                                        "model": "Delta-7 Aethersprite-class interceptor"
                                    }
                                },
                                {
                                    "node": {
                                        "name": "Trade Federation cruiser",
                                        "model": "Providence-class carrier/destroyer"
                                    }
                                },
                                {
                                    "node": {
                                        "name": "Naboo star skiff",
                                        "model": "J-type star skiff"
                                    }
                                },
                                {
                                    "node": {
                                        "name": "Jedi Interceptor",
                                        "model": "Eta-2 Actis-class light interceptor"
                                    }
                                },
                                {
                                    "node": {
                                        "name": "Belbullab-22 starfighter",
                                        "model": "Belbullab-22 starfighter"
                                    }
                                }
                            ]
                        }
                    }
                }
            }""";
        public static final String person10Query = """
            {
              person(personID: 10) {
                name
                birthYear
                gender
                height
                mass
                starshipConnection(first: 10) {
                  edges {
                    node {
                      name
                      model
                    }
                  }
                }
              }
            }""";

    public static final String personTemplate = """
            query queryNayDeOnTap($id: ID)
            {
              person(personID: $id) {
                name
                birthYear
                gender
                height
                mass
                starshipConnection(first: 10) {
                  edges {
                    node {
                      name
                      model
                    }
                  }
                }
              }
            }""";
}
