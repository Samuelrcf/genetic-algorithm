package application;

import java.util.Random;

public class AlgoritmoGenetico {

    private static final int POPULATION_SIZE = 4; // Tamanho da população
    private static final int CHROMOSOME_LENGTH = 6; // 3 bits para x e 3 bits para y
    private static final Random random = new Random();

    public static class Individual {
        String chromosome; // Representa o cromossomo binário

        public Individual(String chromosome) {
            this.chromosome = chromosome;
        }

        @Override
        public String toString() {
            return chromosome;
        }
    }

    // Função de avaliação g(x, y) = x^3 + 2y^4 + 1
    public static double evaluateFitness(Individual individual) {
        String chromosome = individual.chromosome;
        String xBinary = chromosome.substring(0, 3); // Primeiros 3 bits
        String yBinary = chromosome.substring(3); // Últimos 3 bits

        int x = Integer.parseInt(xBinary, 2);
        int y = Integer.parseInt(yBinary, 2);

        return Math.pow(x, 3) + 2 * Math.pow(y, 4) + 1;
    }

    // Calcula a probabilidade invertida de cada indivíduo
    public static double[] calculateProbabilities(Individual[] population) {
        double[] invertedFitness = new double[POPULATION_SIZE];
        double totalInvertedFitness = 0.0;

        // Calcula a aptidão invertida e a soma total
        for (int i = 0; i < POPULATION_SIZE; i++) {
            double fitness = evaluateFitness(population[i]); // Obtém o valor de aptidão de cada cromossomo
            invertedFitness[i] = 1.0 / fitness; // Inverte a aptidão. Ex: 1/10 = 0.1, supondo que o indivíduo teve aptidão de 0.1
            totalInvertedFitness += invertedFitness[i]; // Soma as inverções para fazer a normalização. Ex: 1.35
        }

        // Normalização
        double[] probabilities = new double[POPULATION_SIZE];
        for (int i = 0; i < POPULATION_SIZE; i++) {
            probabilities[i] = invertedFitness[i] / totalInvertedFitness; // Atribuição das probabilidades de cada indivíduo. Ex: 0.1 / 1.35
        }
        
        return probabilities;
    }
    
    // Seleciona um indivíduo usando a roleta
    public static Individual rouletteSelection(Individual[] population, double[] probabilities) {
        double rand = random.nextDouble(); // Gera um número aleatório entre 0 e 1
        double cumulativeProbability = 0.0;
        int selectedIndex = -1; // Índice do indivíduo selecionado

        for (int i = 0; i < POPULATION_SIZE; i++) {
            cumulativeProbability += probabilities[i]; // Acumulação das probabilidades para criar intervalos
                                                        // proporcionais (faixas de proporção para cada indivíduo,
                                                        // pedaços da roleta)
            if (rand <= cumulativeProbability) {
                selectedIndex = i;
                break; // Encontra o indivíduo baseado na probabilidade
            }
        }

        if (selectedIndex == -1) {
            // Se nenhum indivíduo foi selecionado, retorna o indivíduo com a maior probabilidade
            double maxProbability = -1;
            for (int i = 0; i < POPULATION_SIZE; i++) {
                if (probabilities[i] > maxProbability) {
                    maxProbability = probabilities[i];
                    selectedIndex = i;
                }
            }
        }

        return population[selectedIndex];
    }

    // Métodos auxiliares para inicializar a população
    private static Individual[] initializePopulation() {
        Individual[] population = new Individual[POPULATION_SIZE];
        for (int i = 0; i < POPULATION_SIZE; i++) {
            String chromosome = generateRandomChromosome();
            population[i] = new Individual(chromosome);
        }
        return population;
    }

    private static String generateRandomChromosome() {
        StringBuilder chromosome = new StringBuilder();
        for (int i = 0; i < CHROMOSOME_LENGTH; i++) {
            chromosome.append(random.nextInt(2)); // Gera 0 ou 1 aleatoriamente
        }
        return chromosome.toString();
    }

    // Aplica cruzamento de um ponto
    public static Individual[] crossover(Individual parent1, Individual parent2) {
        int crossoverPoint = random.nextInt(CHROMOSOME_LENGTH - 1) + 1; // Escolhe ponto de cruzamento aleatório, 1-5. Não pode ser o cromossomo inteiro porque só usaria o cromossomo de 1 pai
        String parent1Chromosome = parent1.chromosome;
        String parent2Chromosome = parent2.chromosome;

        /*Filho 1: Recebe a primeira parte do cromossomo do Pai 1 (até o ponto de cruzamento) e a segunda parte do cromossomo do Pai 2 (a partir do ponto de cruzamento).
        Filho 2: Recebe a primeira parte do cromossomo do Pai 2 e a segunda parte do cromossomo do Pai 1.*/
        String child1Chromosome = parent1Chromosome.substring(0, crossoverPoint) + parent2Chromosome.substring(crossoverPoint);
        String child2Chromosome = parent2Chromosome.substring(0, crossoverPoint) + parent1Chromosome.substring(crossoverPoint);

        return new Individual[] { new Individual(child1Chromosome), new Individual(child2Chromosome) };
    }

    // Aplica mutação com uma certa taxa
    public static Individual mutate(Individual individual, double mutationRate) {
        StringBuilder chromosome = new StringBuilder(individual.chromosome);
        for (int i = 0; i < CHROMOSOME_LENGTH; i++) {
            if (random.nextDouble() < mutationRate) { // Verifica se o bit deve ser mutado comparando o valor double gerado com o valor da mutação
                chromosome.setCharAt(i, chromosome.charAt(i) == '0' ? '1' : '0'); // Inverte o bit
            }
        }
        return new Individual(chromosome.toString());
    }

    public static Individual[] generateNewGeneration(Individual[] population, double[] probabilities, double mutationRate) {
        Individual[] newPopulation = new Individual[POPULATION_SIZE];

        for (int i = 0; i < 2; i++) {
            Individual parent1 = rouletteSelection(population, probabilities); // Obtém o primeiro pai
            Individual parent2 = rouletteSelection(population, probabilities); // Obtém o segundo pai

            // Aplicar cruzamento
            Individual[] children = crossover(parent1, parent2);

            // Aplicar mutação
            // Coloca os novos indivíduos nas posições 1 e 2 no primeiro loop e nas posições 2 e 3 no segundo loop 
            newPopulation[2 * i] = mutate(children[0], mutationRate); 
            newPopulation[2 * i + 1] = mutate(children[1], mutationRate);
        }

        return newPopulation;
    }

    public static void main(String[] args) {
        Individual[] population = initializePopulation();
        double mutationRate = 0.05; // Quanto menor, menos chances de mutação
        int generationCount = 0;
        Individual bestIndividual = null;
        
        // Variáveis para armazenar o melhor indivíduo global e a geração correspondente
        Individual bestGlobalIndividual = null;
        double bestGlobalFitness = Double.MAX_VALUE; // Inicializa com o maior valor possível
        int bestGlobalGeneration = -1; // Inicializa com -1 para indicar que ainda não foi encontrado

        while (generationCount < 15) {
            generationCount++;
            System.out.println("Geração " + generationCount + ":");

            // Avalia e imprime a aptidão e as probabilidades de cada indivíduo
            double[] probabilities = calculateProbabilities(population);

            bestIndividual = population[0]; // Usado como referência para iniciar o melhor indivíduo, representa o código binário
            double bestFitness = evaluateFitness(bestIndividual); // Representa o valor de aptidão do melhor indivíduo

            for (int i = 0; i < POPULATION_SIZE; i++) { // Imprimindo os cromossomos e respectivo valor de aptidão
                double fitness = evaluateFitness(population[i]);
                double probability = probabilities[i];
                System.out.println("Cromossomo: " + population[i] + " -> Aptidão: " + fitness + " -> Probabilidade: " + probability);

                if (fitness < bestFitness) { // Lógica para obter o melhor indivíduo da geração corrente
                    bestFitness = fitness;
                    bestIndividual = population[i];
                }

                // Atualiza o melhor indivíduo global se este for melhor
                if (fitness < bestGlobalFitness) {
                    bestGlobalFitness = fitness;
                    bestGlobalIndividual = population[i];
                    bestGlobalGeneration = generationCount; // Armazena a geração correspondente
                }
            }

            System.out.println("Melhor indivíduo da geração " + generationCount + ": " + bestIndividual + " com aptidão " + bestFitness);

            if (bestFitness == 1.0) {
                System.out.println("Encontrado mínimo global: " + bestIndividual + " com aptidão " + bestFitness);
                break;
            }

            // Gera a nova geração
            Individual[] newPopulation = generateNewGeneration(population, probabilities, mutationRate);
            population = newPopulation;
        }

        if (bestGlobalIndividual != null && bestGlobalFitness != 1.0) { // Fora do while
            System.out.println("Não encontrou o mínimo global em 15 gerações.");
            System.out.println("O melhor indivíduo global estava na geração " + bestGlobalGeneration + " com cromossomo " + bestGlobalIndividual + " e com aptidão " + bestGlobalFitness);
        }
    }

}
