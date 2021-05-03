package com.aniketbhoite.assume


import com.aniketbhoite.assume.processor.AssumeProcessor
import com.google.common.truth.Truth.assertThat
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile


class AssumeProcessorTest {
    //@Test
    fun testAssumeProcessor() {
        val result = KotlinCompilation().apply {
            sources = listOf(
                SourceFile.kotlin(
                    "NewsApiService.kt", """
                            import retrofit2.http.GET
    import com.aniketbhoite.assume.annotations.Assume

                        interface NewsApiService {
                            @GET("top-headlines?pageSize=100")
                            @Assume(
                                responseCode = 200,
                                response =
                                "{\"status\":\"ok\",\"totalResults\":70,\"articles\":[{\"source\":{\"id\":null,\"name\":\"NYC\"},\"author\":\"Bloomberg\",\"title\":\"Rich Indians flee by private jet as Covid-19 infections spiral - Hindustan Times\",\"description\":\"With reports of hospital bed and drug shortages sweeping social media, Indian tycoons and others able to afford fares running into millions of rupees are booking flights to boltholes in Europe, the Middle East and the Indian Ocean.\",\"url\":\"https://www.hindustantimes.com/india-news/rich-indians-flee-by-private-jet-as-covid-19-infections-spiral-101619448267544.html\",\"urlToImage\":\"https://images.hindustantimes.com/img/2021/04/26/1600x900/approaches-american-airlines-national-landing-airport-washington_c3337c54-5f20-11e9-bb04-32a78a0b0bbe_1619448509890.jpg\",\"publishedAt\":\"2021-04-26T14:50:40Z\",\"content\":\"Indias mounting crisis surrounding a surge in coronavirus infections is prompting wealthy families to flee the country by private jet.\\r\\nWith reports of hospital bed and drug shortages sweeping social… [+2429 chars]\"},{\"source\":{\"id\":null,\"name\":\"Livemint\"},\"author\":\"Shayan Ghosh\",\"title\":\"If left unchecked, second Covid-19 wave could be inflationary: RBI - Mint\",\"description\":\"Pandemic protocols, speedier vaccination, ramping up hospital and ancillary capacity, and remaining resolutely focused on a post-pandemic future of strong and sustainable growth with macroeconomic, financial stability is the way forward, says RBI\",\"url\":\"https://www.livemint.com/economy/left-unchecked-second-coivd-19-wave-could-be-inflationary-rbi-11619447208219.html\",\"urlToImage\":\"https://images.livemint.com/img/2021/04/26/600x338/3c5caacc-9e05-11eb-bcc6-91c576c9961b_1618514778038_1619447315261.jpg\",\"publishedAt\":\"2021-04-26T14:33:15Z\",\"content\":\"MUMBAI :\\r\\nThe second wave of covid-19 in India, if left uncontrolled, could lead to prolonged restrictions on movement and supply chain disruptions with consequent inflationary pressures, the Reserve… [+3949 chars]\"},{\"source\":{\"id\":null,\"name\":\"Hindustan Times\"},\"author\":\"hindustantimes.com\",\"title\":\"Gap between Sputnik-V doses can be increased from 3 weeks to 3 months, say makers - Hindustan Times\",\"description\":\"The makers of the vaccine, Gamaleya Research Centre, said that the interval may even prolong the effect of the vaccine and won't interfere with immune response spurred by the vaccine.\",\"url\":\"https://www.hindustantimes.com/world-news/gap-between-sputnik-v-doses-can-be-increased-say-makers-101619445514281.html\",\"urlToImage\":\"https://images.hindustantimes.com/img/2021/04/26/1600x900/2021-04-24T154931Z_720167739_RC2F2N9NS4ED_RTRMADP_3_HEALTH-CORONAVIRUS-VENEZUELA-RUSSIA_1619445749903_1619445973092.jpg\",\"publishedAt\":\"2021-04-26T14:08:09Z\",\"content\":\"The director of Russias Gamaleya Research Centre, which developed the Sputnik V vaccine against coronavirus disease (Covid-19), said it is possible to increase the minimum interval between the first … [+1834 chars]\"},{\"source\":{\"id\":null,\"name\":\"CarToq.com\"},\"author\":\"Paarth Khatri\",\"title\":\"Suzuki launches the new generation Hayabusa for Rs. 16.40 lakhs ex-showroom - CarToq.com\",\"description\":\"Suzuki has finally launched the much-awaited Hayabusa in the Indian market. The motorcycle has been priced at Rs. 16.40 lakhs ex-showroom which is Rs. 3 lakhs pricier than the previous one that was priced at Rs. 13.75 lakhs ex-showroom. The booking amount for…\",\"url\":\"https://www.cartoq.com/suzuki-launches-the-new-generation-hayabusa-for-rs-16-40-lakhs-ex-showroom/\",\"urlToImage\":\"https://www.cartoq.com/wp-content/uploads/2021/04/Suzuki-Hayabusa-featured-1019x530.jpg\",\"publishedAt\":\"2021-04-26T13:57:14Z\",\"content\":\"Suzuki has finally launched the much-awaited Hayabusa in the Indian market. The motorcycle has been priced at Rs. 16.40 lakhs ex-showroom which is Rs. 3 lakhs pricier than the previous one that was p… [+3284 chars]\"}]}"
                            )
                            fun getArticlesByCateGoryAsync(
                            ): String
                }
            """
                ))

//            inheritClassPath = true
            annotationProcessors = listOf(AssumeProcessor())
            messageOutputStream = System.out
        }.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        // Test diagnostic output of compiler
//        assertThat(result.messages).contains("My annotation processor was called")

        val kClazz = result.classLoader.loadClass("AssumeClass")
        assertThat(kClazz)


    }
}