package spring.project.Screenmatch_API.principal;

import spring.project.Screenmatch_API.model.DadosEpisodio;
import spring.project.Screenmatch_API.model.DadosSerie;
import spring.project.Screenmatch_API.model.DadosTemporada;
import spring.project.Screenmatch_API.model.Episodio;
import spring.project.Screenmatch_API.service.ConsumoApi;
import spring.project.Screenmatch_API.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private final Scanner leitura = new Scanner(System.in);
    private final ConsumoApi api = new ConsumoApi();
    private final ConverteDados conversor = new ConverteDados();

    public void exibeMenu() {
        System.out.println("Digite o nome da série para ser buscada:");
        String nomeSerie = leitura.nextLine();
        String ENDERECO = "https://www.omdbapi.com/?t=";
        String API_KEY = "&apikey=6f25db03";
        var json = api.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);
        List<DadosTemporada> temporadas = new ArrayList<>();
        for (int i = 1; i <= dados.totalTemporadas(); i++) {
            json = api.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);   //"https://www.omdbapi.com/?t=gilmore+girls&season=" + i + "&apikey=6f25db03");
            DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }

        temporadas.forEach(System.out::println);

        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

        List<DadosEpisodio> dadosEpisodios = temporadas.stream().flatMap(t -> t.episodios().stream()).toList();

        System.out.println("\n Top 5 episódios");

        dadosEpisodios.stream().filter(e -> !e.avaliacao().equalsIgnoreCase("N/A")).sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed()).limit(5).forEach(System.out::println);

        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(), d)))
                .toList();

        episodios.forEach(System.out::println);

        System.out.println("Digite um trecho do título do episódio");

        String trechoTitulo = leitura.nextLine();


        Optional<Episodio> episodioBuscado = episodios.stream().filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase())).findFirst();

        if(episodioBuscado.isPresent()){
            System.out.println("Episódio encontrado!");
            System.out.println("Temporada " + episodioBuscado.get().getTemporada());
        } else {
            System.out.println("Episódio não encontrado!");
        }


        System.out.println("A partir de que ano você deseja ver o episódio?");
        var ano = leitura.nextInt();
        leitura.nextLine();

        LocalDate dataBusca = LocalDate.of(ano, 1, 1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        episodios.stream().filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca)).forEach(e -> System.out.println("Temporada: " + e.getTemporada() + " Episodio: " + e.getTitulo() + " Data lançamento: " + e.getDataLancamento().format(formatter)));

        Map<Integer, Double> avaliacaoPorTemporada = episodios.stream().filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada, Collectors.averagingDouble(Episodio::getAvaliacao)));

        System.out.println(avaliacaoPorTemporada);

        DoubleSummaryStatistics est = episodios.stream().filter(e -> e.getAvaliacao() > 0.0).collect(Collectors.summarizingDouble(Episodio::getAvaliacao));

        System.out.println("Média: " + est.getAverage());
        System.out.println("Melhor episódio: " + est.getMax());
        System.out.println("Pior episódio: " + est.getMin());
        System.out.println("Quantiadde: " + est.getCount());

    }
}
